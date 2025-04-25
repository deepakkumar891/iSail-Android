package com.drm.isail.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drm.isail.data.model.LandAssignment
import com.drm.isail.data.model.ShipAssignment
import com.drm.isail.data.model.User
import com.drm.isail.data.model.UserStatus
import com.drm.isail.data.repository.LandAssignmentRepository
import com.drm.isail.data.repository.ShipAssignmentRepository
import com.drm.isail.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val shipAssignmentRepository: ShipAssignmentRepository,
    private val landAssignmentRepository: LandAssignmentRepository
) : ViewModel() {
    
    private val _homeState = MutableStateFlow<HomeState>(HomeState.Loading)
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _shipAssignments = MutableStateFlow<List<ShipAssignment>>(emptyList())
    val shipAssignments: StateFlow<List<ShipAssignment>> = _shipAssignments.asStateFlow()
    
    private val _landAssignments = MutableStateFlow<List<LandAssignment>>(emptyList())
    val landAssignments: StateFlow<List<LandAssignment>> = _landAssignments.asStateFlow()
    
    init {
        loadCurrentUser()
    }
    
    private fun loadCurrentUser() {
        viewModelScope.launch {
            val firebaseUser = userRepository.getCurrentUser()
            
            if (firebaseUser?.userIdentifier != null) {
                userRepository.getUserByFirebaseUid(firebaseUser.userIdentifier!!)
                    .firstOrNull()?.let { user ->
                        _currentUser.value = user
                        loadUserAssignments(user)
                        _homeState.value = HomeState.Success
                    } ?: run {
                        _homeState.value = HomeState.Error("User not found")
                    }
            } else {
                _homeState.value = HomeState.Error("Not logged in")
            }
        }
    }
    
    private fun loadUserAssignments(user: User) {
        viewModelScope.launch {
            if (user.userIdentifier != null) {
                // Load assignments from local database
                shipAssignmentRepository.getShipAssignmentsByFirebaseUid(user.userIdentifier!!)
                    .firstOrNull()?.let { assignments ->
                        _shipAssignments.value = assignments
                    }
                
                landAssignmentRepository.getLandAssignmentsByFirebaseUid(user.userIdentifier!!)
                    .firstOrNull()?.let { assignments ->
                        _landAssignments.value = assignments
                    }
                
                // Sync with Firestore
                syncAssignmentsWithFirestore()
            }
        }
    }
    
    fun syncAssignmentsWithFirestore() {
        viewModelScope.launch {
            shipAssignmentRepository.syncShipAssignmentsWithFirestore()
            landAssignmentRepository.syncLandAssignmentsWithFirestore()
            loadCurrentUser() // Reload data after sync
        }
    }
    
    fun signOut() {
        userRepository.signOut()
        _currentUser.value = null
        _homeState.value = HomeState.SignedOut
    }
    
    fun getCurrentUserStatus(): UserStatus {
        return currentUser.value?.currentStatus ?: UserStatus.ON_LAND
    }
}

sealed class HomeState {
    object Loading : HomeState()
    object Success : HomeState()
    object SignedOut : HomeState()
    data class Error(val message: String) : HomeState()
} 