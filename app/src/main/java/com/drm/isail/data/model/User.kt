package com.drm.isail.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class UserStatus {
    ON_SHIP,
    ON_LAND
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // User details
    var name: String? = null,
    var surname: String? = null,
    var email: String? = null,
    var password: String? = null,
    var mobileNumber: String? = null,
    var fleetWorking: String? = null,
    var presentRank: String? = null,
    
    // Privacy settings
    var isProfileVisible: Boolean = true,
    var showEmailToOthers: Boolean = true,
    var showPhoneToOthers: Boolean = true,
    
    var currentStatus: UserStatus = UserStatus.ON_LAND,
    
    // Firebase Auth identifier
    var userIdentifier: String? = null,
    
    // Company information
    var company: String? = null,
    
    // Photo URL from Firebase Storage
    var photoURL: String? = null
) 