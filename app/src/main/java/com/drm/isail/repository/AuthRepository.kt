package com.drm.isail.repository

import com.drm.isail.model.User
import com.drm.isail.util.UiState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun login(email: String, password: String): Flow<UiState<User>>
    fun register(email: String, password: String, username: String): Flow<UiState<User>>
    fun forgotPassword(email: String): Flow<UiState<String>>
    fun logout(): Flow<UiState<String>>
    fun getCurrentUser(): Flow<UiState<User>>
    fun updateProfile(user: User): Flow<UiState<User>>
} 