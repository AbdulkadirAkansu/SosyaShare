package com.akansu.sosyashare.data.di


import com.akansu.sosyashare.data.repository.UserPrivacyRepositoryImpl
import com.akansu.sosyashare.domain.repository.UserPrivacyRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindUserPrivacyRepository(
        userPrivacyRepositoryImpl: UserPrivacyRepositoryImpl
    ): UserPrivacyRepository
}
