package com.drm.isail.ui.screens.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drm.isail.data.model.LandAssignment
import com.drm.isail.data.model.User
import com.drm.isail.data.repository.LandAssignmentRepository
import com.drm.isail.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject
import kotlin.text.Typography.dagger

@HiltViewModel
class LandFormViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val landAssignmentRepository: LandAssignmentRepository
) : ViewModel() {
    
    private val _formState = MutableStateFlow<LandFormState>(LandFormState.Idle)
    val formState: StateFlow<LandFormState> = _formState.asStateFlow()
    
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
                        _formState.value = LandFormState.Error("User not found")
                    }
            } else {
                _formState.value = LandFormState.Error("Not logged in")
            }
        }
    }
    
    fun createLandAssignment(
        dateHome: Date,
        expectedJoiningDate: Date,
        fleetType: String,
        lastVessel: String,
        email: String,
        mobileNumber: String,
        isPublic: Boolean,
        company: String
    ) {
        val currentUserValue = _currentUser.value ?: run {
            _formState.value = LandFormState.Error("User not authenticated")
            return
        }
        
        if (fleetType.isBlank() || expectedJoiningDate == null) {
            _formState.value = LandFormState.Error("Fleet type and expected joining date are required")
            return
        }
        
        _formState.value = LandFormState.Loading
        
        val landAssignment = LandAssignment(
            userId = currentUserValue.id,
            dateHome = dateHome,
            expectedJoiningDate = expectedJoiningDate,
            fleetType = fleetType,
            lastVessel = lastVessel,
            email = email,
            mobileNumber = mobileNumber,
            isPublic = isPublic,
            company = company,
            userIdentifier = currentUserValue.userIdentifier
        ).apply {
            user = currentUserValue
        }
        
        viewModelScope.launch {
            landAssignmentRepository.createLandAssignment(landAssignment, currentUserValue)
                .onSuccess {
                    _formState.value = LandFormState.Success
                }
                .onFailure { e ->
                    _formState.value = LandFormState.Error(e.message ?: "Failed to create land assignment")
                }
        }
    }
    
    fun resetFormState() {
        _formState.value = LandFormState.Idle
    }
}

sealed class LandFormState {
    object Idle : LandFormState()
    object Loading : LandFormState()
    object Success : LandFormState()
    data class Error(val message: String) : LandFormState()
} 