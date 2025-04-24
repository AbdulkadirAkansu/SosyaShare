@file:Suppress("DEPRECATION")

package com.akansu.sosyashare.data.di

import android.content.Context
import com.akansu.sosyashare.data.remote.*
import com.akansu.sosyashare.data.repository.*
import com.akansu.sosyashare.domain.repository.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore {
        val firestore = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
            .build()
        firestore.firestoreSettings = settings
        return firestore
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = FirebaseStorage.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseAuthService(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        @ApplicationContext context: Context
    ): FirebaseAuthService {
        return FirebaseAuthService(firebaseAuth, firestore, context)
    }

    @Provides
    @Singleton
    fun provideFirebaseUserService(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        firebaseStorage: FirebaseStorage,
        @ApplicationContext context: Context
    ): FirebaseUserService {
        return FirebaseUserService(firebaseAuth, firestore, firebaseStorage, context)
    }

    @Provides
    @Singleton
    fun provideFirebasePostService(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        firebaseStorage: FirebaseStorage,
        @ApplicationContext context: Context
    ): FirebasePostService {
        return FirebasePostService(firebaseAuth, firestore, firebaseStorage, context)
    }

    @Provides
    @Singleton
    fun provideFirebaseStorageService(
        firebaseAuth: FirebaseAuth,
        firebaseStorage: FirebaseStorage,
        @ApplicationContext context: Context
    ): FirebaseStorageService {
        return FirebaseStorageService(firebaseAuth, firebaseStorage, context)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(authService: FirebaseAuthService): AuthRepository {
        return AuthRepositoryImpl(authService)
    }

    @Provides
    @Singleton
    fun provideUserRepository(userService: FirebaseUserService): UserRepository {
        return UserRepositoryImpl(userService)
    }

    @Provides
    @Singleton
    fun providePostRepository(postService: FirebasePostService): PostRepository {
        return PostRepositoryImpl(postService)
    }

    @Provides
    @Singleton
    fun provideStorageRepository(storageService: FirebaseStorageService): StorageRepository {
        return StorageRepositoryImpl(storageService)
    }
}
