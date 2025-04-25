package com.drm.isail.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.drm.isail.data.model.LandAssignment
import kotlinx.coroutines.flow.Flow

@Dao
interface LandAssignmentDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLandAssignment(landAssignment: LandAssignment): Long
    
    @Update
    suspend fun updateLandAssignment(landAssignment: LandAssignment)
    
    @Delete
    suspend fun deleteLandAssignment(landAssignment: LandAssignment)
    
    @Query("SELECT * FROM land_assignments WHERE id = :id")
    fun getLandAssignmentById(id: String): Flow<LandAssignment?>
    
    @Query("SELECT * FROM land_assignments WHERE userId = :userId")
    fun getLandAssignmentsByUserId(userId: String): Flow<List<LandAssignment>>
    
    @Query("SELECT * FROM land_assignments WHERE userIdentifier = :firebaseUid")
    fun getLandAssignmentsByFirebaseUid(firebaseUid: String): Flow<List<LandAssignment>>
    
    @Query("SELECT * FROM land_assignments WHERE isPublic = 1")
    fun getAllPublicLandAssignments(): Flow<List<LandAssignment>>
    
    @Query("DELETE FROM land_assignments WHERE userId = :userId")
    suspend fun deleteAllUserLandAssignments(userId: String)
} 