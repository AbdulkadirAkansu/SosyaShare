@file:Suppress("DEPRECATION")

package com.akansu.sosyashare.data.di

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
            .setPersistenceEnabled(false) // Çevrimdışı veri yönetimini devre dışı bırakın
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
    ): FirebaseAuthService {
        return FirebaseAuthService(firebaseAuth, firestore,)
    }

    @Provides
    @Singleton
    fun provideFirebaseUserService(
        firebaseAuth: FirebaseAuth,
        firestore: FirebaseFirestore,
        firebaseStorage: FirebaseStorage
    ): FirebaseUserService {
        return FirebaseUserService(firebaseAuth, firestore, firebaseStorage)
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
