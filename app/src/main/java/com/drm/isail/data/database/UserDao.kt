package com.drm.isail.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.drm.isail.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUserById(userId: String): Flow<User?>
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    fun getUserByEmail(email: String): Flow<User?>
    
    @Query("SELECT * FROM users WHERE userIdentifier = :firebaseUid LIMIT 1")
    fun getUserByFirebaseUid(firebaseUid: String): Flow<User?>
    
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
} 