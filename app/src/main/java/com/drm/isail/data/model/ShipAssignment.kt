package com.drm.isail.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.Calendar
import java.util.Date
import java.util.UUID

@Entity(tableName = "ship_assignments")
data class ShipAssignment(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Foreign key to User
    var userId: String? = null,
    
    // Assignment details
    var dateOfOnboard: Date? = null,
    var rank: String? = null,
    var shipName: String? = null,
    var company: String? = null,
    var portOfJoining: String? = null,
    var contractLength: Int = 6,
    var email: String? = null,
    var mobileNumber: String? = null,
    var isPublic: Boolean = true,
    var signOffDate: Date? = null,
    var fleetType: String? = null,
    
    // User identifier for device matching
    var userIdentifier: String? = null
) {
    // Calculate expected release date based on contract length
    fun getExpectedReleaseDate(): Date? {
        val onboardDate = dateOfOnboard ?: return Date()
        
        val calendar = Calendar.getInstance()
        calendar.time = onboardDate
        calendar.add(Calendar.MONTH, contractLength)
        
        return calendar.time
    }
    
    // Matching logic with land assignments
    fun matchesWithLandAssignment(landAssignment: LandAssignment): Boolean {
        // Check if we have the necessary data for matching
        val expectedSignOffDate = getExpectedReleaseDate() ?: return false
        val shipFleetType = fleetType ?: return false
        val shipRank = rank?.lowercase() ?: return false
        val landJoiningDate = landAssignment.expectedJoiningDate ?: return false
        val landUser = landAssignment.user ?: return false
        val landRank = landUser.presentRank?.lowercase() ?: return false
        
        // 1. Fleet compatibility - case insensitive comparison
        val landFleet = landAssignment.fleetType?.lowercase() ?: ""
        if (!shipFleetType.lowercase().contains(landFleet) && !landFleet.contains(shipFleetType.lowercase())) {
            return false
        }
        
        // 2. Rank compatibility - case insensitive comparison
        if (landRank.isEmpty() || shipRank.isEmpty() || landRank != shipRank) {
            return false
        }
        
        // 3. Company compatibility (if specified)
        if (!company.isNullOrEmpty()) {
            val landCompany = landAssignment.company?.lowercase() ?: landUser.company?.lowercase() ?: ""
            
            if (!landCompany.isEmpty() && !company!!.lowercase().isEmpty() &&
               landCompany != company!!.lowercase()) {
                return false
            }
        }
        
        // 4. Date compatibility - Check if expected sign-off date is within a reasonable window of the land assignment's expected joining date
        val calendar = Calendar.getInstance()
        
        // Calculate a window of +/- 15 days around the expected sign-off date
        calendar.time = expectedSignOffDate
        calendar.add(Calendar.DAY_OF_MONTH, -15)
        val fifteenDaysBefore = calendar.time
        
        calendar.time = expectedSignOffDate
        calendar.add(Calendar.DAY_OF_MONTH, 15)
        val fifteenDaysAfter = calendar.time
        
        // Check if expected joining date falls within this window around the sign-off date
        return landJoiningDate >= fifteenDaysBefore && landJoiningDate <= fifteenDaysAfter
    }
} 