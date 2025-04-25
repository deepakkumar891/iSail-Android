package com.drm.isail.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.drm.isail.data.model.LandAssignment
import com.drm.isail.data.model.ShipAssignment
import com.drm.isail.data.model.User
import com.drm.isail.util.DateConverter

@Database(
    entities = [User::class, ShipAssignment::class, LandAssignment::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun shipAssignmentDao(): ShipAssignmentDao
    abstract fun landAssignmentDao(): LandAssignmentDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "isail_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 