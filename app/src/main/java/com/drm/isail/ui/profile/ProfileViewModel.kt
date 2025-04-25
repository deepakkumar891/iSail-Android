package com.drm.isail.ui.screens.profile

import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drm.isail.data.model.User
import com.drm.isail.data.repository.AuthRepository
import com.drm.isail.data.repository.UserRepository
import com.drm.isail.util.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val uiState: StateFlow<UiState<User>> = _uiState

    var isUpdating by mutableStateOf(false)
        private set

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId.isNotEmpty()) {
                    userRepository.getUserById(currentUserId).collect { user ->
                        user?.let {
                            _uiState.value = UiState.Success(it)
                        } ?: run {
                            _uiState.value = UiState.Error("User not found")
                        }
                    }
                } else {
                    _uiState.value = UiState.Error("User not authenticated")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading user profile")
                _uiState.value = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    fun updateProfile(user: User) {
        viewModelScope.launch {
            try {
                isUpdating = true
                userRepository.updateUser(user)
                // Refresh profile after update
                loadUserProfile()
            } catch (e: Exception) {
                Timber.e(e, "Error updating profile")
                _uiState.value = UiState.Error(e.message ?: "Failed to update profile")
            } finally {
                isUpdating = false
            }
        }
    }

    fun uploadProfileImage(uri: Uri) {
        viewModelScope.launch {
            try {
                isUpdating = true
                val currentState = uiState.value
                if (currentState is UiState.Success) {
                    val imageUrl = userRepository.uploadProfileImage(uri, currentState.data.id)
                    val updatedUser = currentState.data.copy(profileImageUrl = imageUrl)
                    userRepository.updateUser(updatedUser)
                    loadUserProfile()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error uploading profile image")
                _uiState.value = UiState.Error(e.message ?: "Failed to upload profile image")
            } finally {
                isUpdating = false
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                authRepository.signOut()
            } catch (e: Exception) {
                Timber.e(e, "Error signing out")
            }
        }
    }
} 