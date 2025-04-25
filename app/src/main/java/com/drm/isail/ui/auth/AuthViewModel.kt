package com.drm.isail.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drm.isail.data.model.User
import com.drm.isail.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {
    
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()
    
    private val _registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
    val registerState: StateFlow<RegisterState> = _registerState.asStateFlow()
    
    private val _resetPasswordState = MutableStateFlow<ResetPasswordState>(ResetPasswordState.Idle)
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState.asStateFlow()
    
    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email and password cannot be empty")
            return
        }
        
        _loginState.value = LoginState.Loading
        
        viewModelScope.launch {
            userRepository.loginUser(email, password)
                .onSuccess { user ->
                    _loginState.value = LoginState.Success(user)
                }
                .onFailure { e ->
                    _loginState.value = LoginState.Error(e.message ?: "Login failed")
                }
        }
    }
    
    fun register(
        name: String,
        surname: String,
        email: String,
        password: String,
        mobileNumber: String,
        fleetWorking: String,
        presentRank: String,
        company: String
    ) {
        if (name.isBlank() || surname.isBlank() || email.isBlank() || password.isBlank() || fleetWorking.isBlank() || presentRank.isBlank()) {
            _registerState.value = RegisterState.Error("Please fill all required fields")
            return
        }
        
        _registerState.value = RegisterState.Loading
        
        val newUser = User(
            name = name,
            surname = surname,
            email = email,
            password = password,
            mobileNumber = mobileNumber,
            fleetWorking = fleetWorking,
            presentRank = presentRank,
            company = company
        )
        
        viewModelScope.launch {
            userRepository.registerUser(email, password, newUser)
                .onSuccess { user ->
                    _registerState.value = RegisterState.Success(user)
                }
                .onFailure { e ->
                    _registerState.value = RegisterState.Error(e.message ?: "Registration failed")
                }
        }
    }
    
    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _resetPasswordState.value = ResetPasswordState.Error("Email cannot be empty")
            return
        }
        
        _resetPasswordState.value = ResetPasswordState.Loading
        
        viewModelScope.launch {
            userRepository.resetPassword(email)
                .onSuccess {
                    _resetPasswordState.value = ResetPasswordState.Success
                }
                .onFailure { e ->
                    _resetPasswordState.value = ResetPasswordState.Error(e.message ?: "Password reset failed")
                }
        }
    }
    
    fun resetLoginState() {
        _loginState.value = LoginState.Idle
    }
    
    fun resetRegisterState() {
        _registerState.value = RegisterState.Idle
    }
    
    fun resetResetPasswordState() {
        _resetPasswordState.value = ResetPasswordState.Idle
    }
}

sealed class LoginState {
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success(val user: User) : LoginState()
    data class Error(val message: String) : LoginState()
}

sealed class RegisterState {
    object Idle : RegisterState()
    object Loading : RegisterState()
    data class Success(val user: User) : RegisterState()
    data class Error(val message: String) : RegisterState()
}

sealed class ResetPasswordState {
    object Idle : ResetPasswordState()
    object Loading : ResetPasswordState()
    object Success : ResetPasswordState()
    data class Error(val message: String) : ResetPasswordState()
} 