package com.drm.isail.util

/**
 * Constants used throughout the app
 */
object Constants {
    // Fleet types
    val FLEET_TYPES = listOf(
        "Container", 
        "Tanker", 
        "Bulk Carrier", 
        "RORO", 
        "Cruise", 
        "Offshore", 
        "Bulk & Gear", 
        "Other"
    )
    
    // Default company
    const val DEFAULT_COMPANY = "Anglo Eastern Ship Management"
    
    // Firestore collections
    const val USERS_COLLECTION = "users"
    const val SHIP_ASSIGNMENTS_COLLECTION = "shipAssignments"
    const val LAND_ASSIGNMENTS_COLLECTION = "landAssignments"
    
    // Navigation routes
    object NavRoutes {
        const val LOGIN = "login"
        const val REGISTER = "register"
        const val FORGOT_PASSWORD = "forgotPassword"
        const val HOME = "home"
        const val PROFILE = "profile"
        const val MATCHES = "matches"
        const val SHIP_FORM = "shipForm"
        const val LAND_FORM = "landForm"
        const val SEARCH = "search"
    }
} 