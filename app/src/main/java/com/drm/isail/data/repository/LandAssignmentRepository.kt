package com.drm.isail.data.repository

import com.drm.isail.data.database.LandAssignmentDao
import com.drm.isail.data.model.LandAssignment
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
class LandAssignmentRepository @Inject constructor(
    private val landAssignmentDao: LandAssignmentDao,
    private val userRepository: UserRepository,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // Local database operations
    suspend fun insertLandAssignment(landAssignment: LandAssignment): Long {
        return landAssignmentDao.insertLandAssignment(landAssignment)
    }
    
    suspend fun updateLandAssignment(landAssignment: LandAssignment) {
        landAssignmentDao.updateLandAssignment(landAssignment)
    }
    
    suspend fun deleteLandAssignment(landAssignment: LandAssignment) {
        landAssignmentDao.deleteLandAssignment(landAssignment)
    }
    
    fun getLandAssignmentById(id: String): Flow<LandAssignment?> {
        return landAssignmentDao.getLandAssignmentById(id)
    }
    
    fun getLandAssignmentsByUserId(userId: String): Flow<List<LandAssignment>> {
        return landAssignmentDao.getLandAssignmentsByUserId(userId)
    }
    
    fun getLandAssignmentsByFirebaseUid(firebaseUid: String): Flow<List<LandAssignment>> {
        return landAssignmentDao.getLandAssignmentsByFirebaseUid(firebaseUid)
    }
    
    fun getAllPublicLandAssignments(): Flow<List<LandAssignment>> {
        return landAssignmentDao.getAllPublicLandAssignments()
    }
    
    suspend fun deleteAllUserLandAssignments(userId: String) {
        landAssignmentDao.deleteAllUserLandAssignments(userId)
    }
    
    // Firebase operations
    suspend fun createLandAssignment(landAssignment: LandAssignment, user: User): Result<LandAssignment> {
        return try {
            val userId = user.userIdentifier ?: firebaseAuth.currentUser?.uid
            
            if (userId != null) {
                // Update land assignment with user ID
                landAssignment.userId = user.id
                landAssignment.userIdentifier = userId
                
                // Set user instance for relationship
                landAssignment.user = user
                
                // Save to Firestore
                val docRef = firestore.collection(Constants.LAND_ASSIGNMENTS_COLLECTION)
                    .document(landAssignment.id)
                
                docRef.set(landAssignment).await()
                
                // Save to local database
                insertLandAssignment(landAssignment)
                
                // Update user status to ON_LAND
                user.currentStatus = UserStatus.ON_LAND
                userRepository.updateUser(user)
                
                // Sync user with Firestore
                userRepository.syncUserWithFirestore(user)
                
                Result.success(landAssignment)
            } else {
                Result.failure(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun syncLandAssignmentsWithFirestore(): Result<List<LandAssignment>> {
        return try {
            val userId = firebaseAuth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
            
            // Get all land assignments for the current user
            val landAssignmentsQuery = firestore.collection(Constants.LAND_ASSIGNMENTS_COLLECTION)
                .whereEqualTo("userIdentifier", userId)
                .get()
                .await()
            
            val landAssignments = landAssignmentsQuery.toObjects(LandAssignment::class.java)
            
            // Save to local database
            landAssignments.forEach { landAssignment ->
                insertLandAssignment(landAssignment)
            }
            
            Result.success(landAssignments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getAllPublicLandAssignmentsFromFirestore(): Result<List<LandAssignment>> {
        return try {
            val query = firestore.collection(Constants.LAND_ASSIGNMENTS_COLLECTION)
                .whereEqualTo("isPublic", true)
                .get()
                .await()
            
            val landAssignments = query.toObjects(LandAssignment::class.java)
            Result.success(landAssignments)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun updateLandAssignmentInFirestore(landAssignment: LandAssignment): Result<LandAssignment> {
        return try {
            firestore.collection(Constants.LAND_ASSIGNMENTS_COLLECTION)
                .document(landAssignment.id)
                .set(landAssignment)
                .await()
            
            // Update local database
            updateLandAssignment(landAssignment)
            
            Result.success(landAssignment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteLandAssignmentFromFirestore(landAssignment: LandAssignment): Result<Unit> {
        return try {
            firestore.collection(Constants.LAND_ASSIGNMENTS_COLLECTION)
                .document(landAssignment.id)
                .delete()
                .await()
            
            // Delete from local database
            deleteLandAssignment(landAssignment)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 