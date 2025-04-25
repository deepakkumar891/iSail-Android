package com.drm.isail.data.repository

import com.drm.isail.data.database.ShipAssignmentDao
import com.drm.isail.data.model.ShipAssignment
import com.drm.isail.data.model.User
import com.drm.isail.data.model.UserStatus
import com.drm.isail.util.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShipAssignmentRepository @Inject constructor(
    private val shipAssignmentDao: ShipAssignmentDao,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // Local database operations
    suspend fun insertShipAssignment(shipAssignment: ShipAssignment): Long {
        return shipAssignmentDao.insertShipAssignment(shipAssignment)
    }
    
    suspend fun updateShipAssignment(shipAssignment: ShipAssignment) {
        shipAssignmentDao.updateShipAssignment(shipAssignment)
    }
    
    suspend fun deleteShipAssignment(shipAssignment: ShipAssignment) {
        shipAssignmentDao.deleteShipAssignment(shipAssignment)
    }
    
    fun getShipAssignmentById(id: String): Flow<ShipAssignment?> {
        return shipAssignmentDao.getShipAssignmentById(id)
    }
    
    fun getShipAssignmentsByUserId(userId: String): Flow<List<ShipAssignment>> {
        return shipAssignmentDao.getShipAssignmentsByUserId(userId)
    }
    
    fun getShipAssignmentsByFirebaseUid(firebaseUid: String): Flow<List<ShipAssignment>> {
        return shipAssignmentDao.getShipAssignmentsByFirebaseUid(firebaseUid)
    }
    
    fun getAllPublicShipAssignments(): Flow<List<ShipAssignment>> {
        return shipAssignmentDao.getAllPublicShipAssignments()
    }
    
    suspend fun deleteAllUserShipAssignments(userId: String) {
        shipAssignmentDao.deleteAllUserShipAssignments(userId)
    }
    
    // Firebase operations
    suspend fun createShipAssignment(shipAssignment: ShipAssignment, user: User): Result<ShipAssignment> {
        return try {
            val userId = user.userIdentifier ?: firebaseAuth.currentUser?.uid
            
            if (userId != null) {
                // Update ship assignment with user ID
                shipAssignment.userId = user.id
                shipAssignment.userIdentifier = userId
                
                // Save to Firestore
                val docRef = firestore.collection(Constants.SHIP_ASSIGNMENTS_COLLECTION)
                    .document(shipAssignment.id)
                
                docRef.set(shipAssignment).await()
                
                // Save to local database
                insertShipAssignment(shipAssignment)
                
                // Update user status to ON_SHIP
                user.currentStatus = UserStatus.ON_SHIP
                userRepository.updateUser(user)
                
                // Sync user with Firestore
                userRepository.syncUserWithFirestore(user)
                
                Result.success(shipAssignment)
            } else {
                Result.failure(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncShipAssignmentsWithFirestore(): Result<List<ShipAssignment>> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            
            // Get all ship assignments for the current user
            val shipAssignmentsQuery = firestore.collection(Constants.SHIP_ASSIGNMENTS_COLLECTION)
                .whereEqualTo("userIdentifier", userId)
                .get()
                .await()
            
            val shipAssignments = shipAssignmentsQuery.toObjects(ShipAssignment::class.java)
            
            // Save to local database
            shipAssignments.forEach { shipAssignment ->
                insertShipAssignment(shipAssignment)
            }
            
            Result.success(shipAssignments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllPublicShipAssignmentsFromFirestore(): Result<List<ShipAssignment>> {
        return try {
            val query = firestore.collection(Constants.SHIP_ASSIGNMENTS_COLLECTION)
                .whereEqualTo("isPublic", true)
                .get()
                .await()
            
            val shipAssignments = query.toObjects(ShipAssignment::class.java)
            Result.success(shipAssignments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateShipAssignmentInFirestore(shipAssignment: ShipAssignment): Result<ShipAssignment> {
        return try {
            firestore.collection(Constants.SHIP_ASSIGNMENTS_COLLECTION)
                .document(shipAssignment.id)
                .set(shipAssignment)
                .await()
            
            // Update local database
            updateShipAssignment(shipAssignment)
            
            Result.success(shipAssignment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteShipAssignmentFromFirestore(shipAssignment: ShipAssignment): Result<Unit> {
        return try {
            firestore.collection(Constants.SHIP_ASSIGNMENTS_COLLECTION)
                .document(shipAssignment.id)
                .delete()
                .await()
            
            // Delete from local database
            deleteShipAssignment(shipAssignment)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 