package com.drm.isail.di

import android.content.Context
import com.drm.isail.data.database.AppDatabase
import com.drm.isail.data.database.LandAssignmentDao
import com.drm.isail.data.database.ShipAssignmentDao
import com.drm.isail.data.database.UserDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    // Firebase
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
    
    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = FirebaseFirestore.getInstance()
    
    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()
    
    // Database
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideUserDao(appDatabase: AppDatabase): UserDao {
        return appDatabase.userDao()
    }
    
    @Provides
    fun provideShipAssignmentDao(appDatabase: AppDatabase): ShipAssignmentDao {
        return appDatabase.shipAssignmentDao()
    }
    
    @Provides
    fun provideLandAssignmentDao(appDatabase: AppDatabase): LandAssignmentDao {
        return appDatabase.landAssignmentDao()
    }
} 