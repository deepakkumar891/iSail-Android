package com.drm.isail.ui.screens.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drm.isail.data.model.ShipAssignment
import com.drm.isail.data.model.User
import com.drm.isail.data.repository.ShipAssignmentRepository
import com.drm.isail.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class ShipFormViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val shipAssignmentRepository: ShipAssignmentRepository
) : ViewModel() {
    
    private val _formState = MutableStateFlow<ShipFormState>(ShipFormState.Idle)
    val formState: StateFlow<ShipFormState> = _formState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
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
                    } ?: run {
                        _formState.value = ShipFormState.Error("User not found")
                    }
            } else {
                _formState.value = ShipFormState.Error("Not logged in")
            }
        }
    }
    
    fun createShipAssignment(
        dateOfOnboard: Date,
        rank: String,
        shipName: String,
        company: String,
        portOfJoining: String,
        contractLength: Int,
        email: String,
        mobileNumber: String,
        isPublic: Boolean,
        fleetType: String
    ) {
        val currentUserValue = _currentUser.value ?: run {
            _formState.value = ShipFormState.Error("User not authenticated")
            return
        }
        
        if (shipName.isBlank() || rank.isBlank() || fleetType.isBlank()) {
            _formState.value = ShipFormState.Error("Ship name, rank, and fleet type are required")
            return
        }
        
        _formState.value = ShipFormState.Loading
        
        val shipAssignment = ShipAssignment(
            userId = currentUserValue.id,
            dateOfOnboard = dateOfOnboard,
            rank = rank,
            shipName = shipName,
            company = company,
            portOfJoining = portOfJoining,
            contractLength = contractLength,
            email = email,
            mobileNumber = mobileNumber,
            isPublic = isPublic,
            fleetType = fleetType,
            userIdentifier = currentUserValue.userIdentifier
        )
        
        viewModelScope.launch {
            shipAssignmentRepository.createShipAssignment(shipAssignment, currentUserValue)
                .onSuccess {
                    _formState.value = ShipFormState.Success
                }
                .onFailure { e ->
                    _formState.value = ShipFormState.Error(e.message ?: "Failed to create ship assignment")
                }
        }
    }
    
    fun resetFormState() {
        _formState.value = ShipFormState.Idle
    }
}

sealed class ShipFormState {
    object Idle : ShipFormState()
    object Loading : ShipFormState()
    object Success : ShipFormState()
    data class Error(val message: String) : ShipFormState()
} 