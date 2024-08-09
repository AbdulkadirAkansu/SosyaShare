package com.akansu.sosyashare.data.di

import com.akansu.sosyashare.data.local.UserDao
import com.akansu.sosyashare.data.remote.*
import com.akansu.sosyashare.data.repository.*
import com.akansu.sosyashare.domain.repository.*
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideFirebaseAuthService(userDao: UserDao): FirebaseAuthService {
        val firebaseStorage = FirebaseStorage.getInstance()
        return FirebaseAuthService(userDao,firebaseStorage)
    }

    @Provides
    @Singleton
    fun provideFirebaseUserService(userDao: UserDao, firebaseStorage: FirebaseStorage): FirebaseUserService {
        return FirebaseUserService(userDao, firebaseStorage)
    }

    @Provides
    @Singleton
    fun provideFirebaseStorageService(): FirebaseStorageService {
        return FirebaseStorageService()
    }

    @Provides
    @Singleton
    fun provideFirebasePostService(): FirebasePostService {
        val firebaseStorage = FirebaseStorage.getInstance()
        return FirebasePostService(firebaseStorage)
    }

    @Provides
    @Singleton
    fun provideAuthRepository(authService: FirebaseAuthService): AuthRepository {
        return AuthRepositoryImpl(authService)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userService: FirebaseUserService
    ): UserRepository {
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

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }
}
