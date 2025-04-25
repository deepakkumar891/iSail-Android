package com.drm.isail.data.repository

import com.drm.isail.data.database.UserDao
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
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // Local database operations
    suspend fun insertUser(user: User): Long {
        return userDao.insertUser(user)
    }
    
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
    
    suspend fun deleteUser(user: User) {
        userDao.deleteUser(user)
    }
    
    fun getUserById(userId: String): Flow<User?> {
        return userDao.getUserById(userId)
    }
    
    fun getUserByEmail(email: String): Flow<User?> {
        return userDao.getUserByEmail(email)
    }
    
    fun getUserByFirebaseUid(firebaseUid: String): Flow<User?> {
        return userDao.getUserByFirebaseUid(firebaseUid)
    }
    
    fun getAllUsers(): Flow<List<User>> {
        return userDao.getAllUsers()
    }
    
    // Firebase operations
    suspend fun registerUser(email: String, password: String, user: User): Result<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                // Update user with Firebase UID
                user.userIdentifier = firebaseUser.uid
                
                // Save user to Firestore
                firestore.collection(Constants.USERS_COLLECTION)
                    .document(firebaseUser.uid)
                    .set(user)
                    .await()
                
                // Save to local database
                insertUser(user)
                
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to create user"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun loginUser(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user
            
            if (firebaseUser != null) {
                // Get user from Firestore
                val userDoc = firestore.collection(Constants.USERS_COLLECTION)
                    .document(firebaseUser.uid)
                    .get()
                    .await()
                
                if (userDoc.exists()) {
                    val userData = userDoc.toObject(User::class.java)
                    if (userData != null) {
                        // Save to local database
                        insertUser(userData)
                        Result.success(userData)
                    } else {
                        Result.failure(Exception("Failed to parse user data"))
                    }
                } else {
                    // Create new user if not in Firestore (fallback)
                    val newUser = User(
                        email = email,
                        userIdentifier = firebaseUser.uid,
                        currentStatus = UserStatus.ON_LAND
                    )
                    
                    // Save to Firestore
                    firestore.collection(Constants.USERS_COLLECTION)
                        .document(firebaseUser.uid)
                        .set(newUser)
                        .await()
                    
                    // Save to local database
                    insertUser(newUser)
                    
                    Result.success(newUser)
                }
            } else {
                Result.failure(Exception("Failed to sign in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun resetPassword(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun signOut() {
        firebaseAuth.signOut()
    }
    
    fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser
        return if (firebaseUser != null) {
            User(
                userIdentifier = firebaseUser.uid,
                email = firebaseUser.email
            )
        } else {
            null
        }
    }
    
    suspend fun syncUserWithFirestore(user: User): Result<User> {
        return try {
            val userId = user.userIdentifier ?: firebaseAuth.currentUser?.uid
            
            if (userId != null) {
                // Update Firestore
                firestore.collection(Constants.USERS_COLLECTION)
                    .document(userId)
                    .set(user)
                    .await()
                
                Result.success(user)
            } else {
                Result.failure(Exception("User not authenticated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 