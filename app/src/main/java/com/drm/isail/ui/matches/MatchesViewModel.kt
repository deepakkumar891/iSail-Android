package com.drm.isail.ui.screens.matches

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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchesViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val shipAssignmentRepository: ShipAssignmentRepository,
    private val landAssignmentRepository: LandAssignmentRepository
) : ViewModel() {
    
    private val _matchesState = MutableStateFlow<MatchesState>(MatchesState.Loading)
    val matchesState: StateFlow<MatchesState> = _matchesState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    private val _shipMatches = MutableStateFlow<List<ShipAssignment>>(emptyList())
    val shipMatches: StateFlow<List<ShipAssignment>> = _shipMatches.asStateFlow()
    
    private val _landMatches = MutableStateFlow<List<LandAssignment>>(emptyList())
    val landMatches: StateFlow<List<LandAssignment>> = _landMatches.asStateFlow()
    
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
                        findMatches(user)
                    } ?: run {
                        _matchesState.value = MatchesState.Error("User not found")
                    }
            } else {
                _matchesState.value = MatchesState.Error("Not logged in")
            }
        }
    }
    
    private fun findMatches(user: User) {
        _matchesState.value = MatchesState.Loading
        
        viewModelScope.launch {
            try {
                // Sync with Firestore first
                shipAssignmentRepository.syncShipAssignmentsWithFirestore()
                landAssignmentRepository.syncLandAssignmentsWithFirestore()
                
                // Get all public assignments
                val publicShipResult = shipAssignmentRepository.getAllPublicShipAssignmentsFromFirestore()
                val publicLandResult = landAssignmentRepository.getAllPublicLandAssignmentsFromFirestore()
                
                if (publicShipResult.isSuccess && publicLandResult.isSuccess) {
                    val publicShipAssignments = publicShipResult.getOrNull() ?: emptyList()
                    val publicLandAssignments = publicLandResult.getOrNull() ?: emptyList()
                    
                    // Get user's assignments
                    val userShipAssignments = if (user.userIdentifier != null) {
                        shipAssignmentRepository.getShipAssignmentsByFirebaseUid(user.userIdentifier!!)
                            .firstOrNull() ?: emptyList()
                    } else emptyList()
                    
                    val userLandAssignments = if (user.userIdentifier != null) {
                        landAssignmentRepository.getLandAssignmentsByFirebaseUid(user.userIdentifier!!)
                            .firstOrNull() ?: emptyList()
                    } else emptyList()
                    
                    when (user.currentStatus) {
                        UserStatus.ON_SHIP -> {
                            // Find land assignments that match the user's ship assignment
                            val matches = mutableListOf<LandAssignment>()
                            userShipAssignments.forEach { shipAssignment ->
                                publicLandAssignments.forEach { landAssignment ->
                                    // Skip user's own land assignments
                                    if (landAssignment.userIdentifier != user.userIdentifier && 
                                        shipAssignment.matchesWithLandAssignment(landAssignment)) {
                                        matches.add(landAssignment)
                                    }
                                }
                            }
                            _landMatches.value = matches
                            _shipMatches.value = emptyList()
                        }
                        UserStatus.ON_LAND -> {
                            // Find ship assignments that match the user's land assignment
                            val matches = mutableListOf<ShipAssignment>()
                            userLandAssignments.forEach { landAssignment ->
                                publicShipAssignments.forEach { shipAssignment ->
                                    // Skip user's own ship assignments
                                    if (shipAssignment.userIdentifier != user.userIdentifier && 
                                        landAssignment.matchesWithShipAssignment(shipAssignment)) {
                                        matches.add(shipAssignment)
                                    }
                                }
                            }
                            _shipMatches.value = matches
                            _landMatches.value = emptyList()
                        }
                    }
                    
                    _matchesState.value = MatchesState.Success
                } else {
                    _matchesState.value = MatchesState.Error("Failed to load matches")
                }
            } catch (e: Exception) {
                _matchesState.value = MatchesState.Error(e.message ?: "Failed to load matches")
            }
        }
    }
    
    fun refreshMatches() {
        currentUser.value?.let { findMatches(it) }
    }
}

sealed class MatchesState {
    object Loading : MatchesState()
    object Success : MatchesState()
    data class Error(val message: String) : MatchesState()
} 