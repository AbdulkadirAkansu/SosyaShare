package com.akansu.sosyashare.data.di

import com.akansu.sosyashare.data.remote.FirebaseBlockedService
import com.akansu.sosyashare.data.repository.BlockedUserRepositoryImpl
import com.akansu.sosyashare.domain.repository.BlockedUserRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BlockedUserModule {

    @Provides
    @Singleton
    fun provideFirebaseBlockedService(
        firestore: FirebaseFirestore
    ): FirebaseBlockedService {
        return FirebaseBlockedService(firestore)
    }

    @Provides
    @Singleton
    fun provideBlockedUserRepository(
        firebaseBlockedService: FirebaseBlockedService
    ): BlockedUserRepository {
        return BlockedUserRepositoryImpl(firebaseBlockedService)
    }
}
