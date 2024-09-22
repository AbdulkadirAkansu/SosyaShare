package com.akansu.sosyashare.data.di

import com.akansu.sosyashare.data.remote.FirebaseMessageService
import com.akansu.sosyashare.data.repository.MessageRepositoryImpl
import com.akansu.sosyashare.domain.repository.MessageRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MessageModule {

    @Provides
    @Singleton
    fun provideMessageService(
        firestore: FirebaseFirestore,
        storage: FirebaseStorage
    ): FirebaseMessageService {
        return FirebaseMessageService(firestore, storage)
    }

    @Provides
    @Singleton
    fun provideMessageRepository(
        messageService: FirebaseMessageService
    ): MessageRepository {
        return MessageRepositoryImpl(messageService)
    }
}
