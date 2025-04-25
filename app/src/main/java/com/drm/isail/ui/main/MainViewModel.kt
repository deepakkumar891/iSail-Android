package com.drm.isail.ui.screens.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drm.isail.data.model.User
import com.drm.isail.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _mainState = MutableStateFlow<MainState>(MainState.Loading)
    val mainState: StateFlow<MainState> = _mainState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()
    
    init {
        checkAuthStatus()
    }
    
    private fun checkAuthStatus() {
        viewModelScope.launch {
            val firebaseUser = userRepository.getCurrentUser()
            
            if (firebaseUser?.userIdentifier != null) {
                userRepository.getUserByFirebaseUid(firebaseUser.userIdentifier!!)
                    .firstOrNull()?.let { user ->
                        _currentUser.value = user
                        _mainState.value = MainState.Authenticated
                    } ?: run {
                        _mainState.value = MainState.Unauthenticated
                    }
            } else {
                _mainState.value = MainState.Unauthenticated
            }
        }
    }
    
    fun signOut() {
        userRepository.signOut()
        _currentUser.value = null
        _mainState.value = MainState.Unauthenticated
    }
}

sealed class MainState {
    object Loading : MainState()
    object Authenticated : MainState()
    object Unauthenticated : MainState()
} 