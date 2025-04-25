package com.drm.isail.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar
import java.util.Date
import java.util.UUID

@Entity(tableName = "land_assignments")
data class LandAssignment(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    // Foreign key to User
    var userId: String? = null,
    
    // Assignment details
    var dateHome: Date? = null,
    var expectedJoiningDate: Date? = null,
    var fleetType: String? = null,
    var lastVessel: String? = null,
    var email: String? = null,
    var mobileNumber: String? = null,
    var isPublic: Boolean = true,
    var company: String? = null,
    
    // User identifier for device matching
    var userIdentifier: String? = null
) {
    // User relation will be handled by Room database
    var user: User? = null
    
    // Matching logic with ship assignments
    fun matchesWithShipAssignment(shipAssignment: ShipAssignment): Boolean {
        // Check if we have the necessary data for matching
        val expectedDate = expectedJoiningDate ?: return false
        val fleetType = this.fleetType ?: return false
        val landUser = this.user ?: return false
        
        // 1. Fleet compatibility - case insensitive comparison
        val shipFleet = shipAssignment.fleetType?.lowercase() ?: ""
        if (!fleetType.lowercase().contains(shipFleet) && !shipFleet.contains(fleetType.lowercase())) {
            return false
        }
        
        // 2. Rank compatibility - case insensitive comparison
        val landRank = landUser.presentRank?.lowercase() ?: ""
        val shipRank = shipAssignment.rank?.lowercase() ?: ""
        if (landRank.isEmpty() || shipRank.isEmpty() || landRank != shipRank) {
            return false
        }
        
        // 3. Company compatibility (if specified)
        if (!shipAssignment.company.isNullOrEmpty()) {
            val landCompany = this.company?.lowercase() ?: landUser.company?.lowercase() ?: ""
            
            if (!landCompany.isEmpty() && !shipAssignment.company!!.lowercase().isEmpty() &&
               landCompany != shipAssignment.company!!.lowercase()) {
                return false
            }
        }
        
        // 4. Date compatibility - Check if release date is within a reasonable window of expected join date
        val shipReleaseDate = shipAssignment.getExpectedReleaseDate() ?: return false
        
        val calendar = Calendar.getInstance()
        
        // Calculate a window of +/- 15 days around the expected joining date
        calendar.time = expectedDate
        calendar.add(Calendar.DAY_OF_MONTH, -15)
        val fifteenDaysBefore = calendar.time
        
        calendar.time = expectedDate
        calendar.add(Calendar.DAY_OF_MONTH, 15)
        val fifteenDaysAfter = calendar.time
        
        // Check if ship release date falls within this window
        return shipReleaseDate >= fifteenDaysBefore && shipReleaseDate <= fifteenDaysAfter
    }
} 