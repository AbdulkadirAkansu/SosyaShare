package com.akansu.sosyashare.data.di

import com.akansu.sosyashare.data.remote.FirebaseSaveService
import com.akansu.sosyashare.data.repository.SaveRepositoryImpl
import com.akansu.sosyashare.domain.repository.SaveRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SaveModule {

    @Provides
    @Singleton
    fun provideFirebaseSaveService(
        firestore: FirebaseFirestore,
        userRepository: UserRepository
    ): FirebaseSaveService {
        return FirebaseSaveService(firestore, userRepository)
    }

    @Provides
    @Singleton
    fun provideSaveRepository(
        saveService: FirebaseSaveService
    ): SaveRepository {
        return SaveRepositoryImpl(saveService)
    }
}
