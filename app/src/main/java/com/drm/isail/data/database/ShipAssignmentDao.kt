package com.drm.isail.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.drm.isail.data.model.ShipAssignment
import kotlinx.coroutines.flow.Flow

@Dao
interface ShipAssignmentDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShipAssignment(shipAssignment: ShipAssignment): Long
    
    @Update
    suspend fun updateShipAssignment(shipAssignment: ShipAssignment)
    
    @Delete
    suspend fun deleteShipAssignment(shipAssignment: ShipAssignment)
    
    @Query("SELECT * FROM ship_assignments WHERE id = :id")
    fun getShipAssignmentById(id: String): Flow<ShipAssignment?>
    
    @Query("SELECT * FROM ship_assignments WHERE userId = :userId")
    fun getShipAssignmentsByUserId(userId: String): Flow<List<ShipAssignment>>
    
    @Query("SELECT * FROM ship_assignments WHERE userIdentifier = :firebaseUid")
    fun getShipAssignmentsByFirebaseUid(firebaseUid: String): Flow<List<ShipAssignment>>
    
    @Query("SELECT * FROM ship_assignments WHERE isPublic = 1")
    fun getAllPublicShipAssignments(): Flow<List<ShipAssignment>>
    
    @Query("DELETE FROM ship_assignments WHERE userId = :userId")
    suspend fun deleteAllUserShipAssignments(userId: String)
} 