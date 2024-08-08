package com.akansu.sosyashare.data.di

import com.akansu.sosyashare.domain.repository.StorageRepository
import com.akansu.sosyashare.domain.repository.UserRepository
import com.akansu.sosyashare.domain.usecase.*
import com.akansu.sosyashare.domain.usecase.profile.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DomainModule {

    @Provides
    @Singleton
    fun provideGetUserDetailsUseCase(userRepository: UserRepository): GetUserDetailsUseCase {
        return GetUserDetailsUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideFollowUserUseCase(userRepository: UserRepository): FollowUserUseCase {
        return FollowUserUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideUnfollowUserUseCase(userRepository: UserRepository): UnfollowUserUseCase {
        return UnfollowUserUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideCheckIfFollowingUseCase(userRepository: UserRepository): CheckIfFollowingUseCase {
        return CheckIfFollowingUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideGetCurrentUserIdUseCase(userRepository: UserRepository): GetCurrentUserIdUseCase {
        return GetCurrentUserIdUseCase(userRepository)
    }

    @Provides
    @Singleton
    fun provideUploadProfilePictureUseCase(storageRepository: StorageRepository): UploadProfilePictureUseCase {
        return UploadProfilePictureUseCase(storageRepository)
    }

    @Provides
    @Singleton
    fun provideGetCurrentUserProfilePictureUrlUseCase(userRepository: UserRepository): GetCurrentUserProfilePictureUrlUseCase {
        return GetCurrentUserProfilePictureUrlUseCase(userRepository)
    }
}
