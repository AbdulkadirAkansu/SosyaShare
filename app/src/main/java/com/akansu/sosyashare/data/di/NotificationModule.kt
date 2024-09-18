package com.akansu.sosyashare.data.di

import com.akansu.sosyashare.data.remote.FirebaseNotificationService
import com.akansu.sosyashare.data.repository.NotificationRepositoryImpl
import com.akansu.sosyashare.domain.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Tüm uygulama boyunca singleton olarak kullanılır
object NotificationModule {

    @Provides
    @Singleton
    fun provideFirebaseNotificationService(firestore: FirebaseFirestore): FirebaseNotificationService {
        return FirebaseNotificationService(firestore)
    }

    @Provides
    @Singleton
    fun provideNotificationRepository(
        firebaseNotificationService: FirebaseNotificationService
    ): NotificationRepository {
        return NotificationRepositoryImpl(firebaseNotificationService)
    }
}
