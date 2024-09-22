package com.akansu.sosyashare.data.di

import com.akansu.sosyashare.data.remote.FirebaseMessagingService
import com.akansu.sosyashare.data.repository.MessagingRepositoryImpl
import com.akansu.sosyashare.domain.repository.MessagingRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseServiceModule {

    @Provides
    @Singleton
    fun provideFirebaseMessagingService(): FirebaseMessagingService {
        return FirebaseMessagingService()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class MessagingRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindMessagingRepository(
        messagingRepositoryImpl: MessagingRepositoryImpl
    ): MessagingRepository
}

