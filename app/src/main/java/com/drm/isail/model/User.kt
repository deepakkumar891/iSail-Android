package com.drm.isail.model

import java.util.Date

data class User(
    val id: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val profilePictureUrl: String? = null,
    val createdAt: Date? = null,
    val updatedAt: Date? = null
) 