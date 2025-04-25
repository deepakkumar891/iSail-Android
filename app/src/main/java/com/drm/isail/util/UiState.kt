package com.drm.isail.util

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<out T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
    
    fun isLoading(): Boolean = this is Loading
    fun isSuccess(): Boolean = this is Success
    fun isError(): Boolean = this is Error
    
    fun getSuccessData(): T? = if (this is Success) data else null
    fun getErrorMessage(): String? = if (this is Error) message else null
} 