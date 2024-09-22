package com.akansu.sosyashare.data.di

import com.akansu.sosyashare.data.remote.FirebaseCommentService
import com.akansu.sosyashare.data.repository.CommentRepositoryImpl
import com.akansu.sosyashare.domain.repository.CommentRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommentModule {

    @Provides
    @Singleton
    fun provideCommentService(): FirebaseCommentService {
        return FirebaseCommentService()
    }

    @Provides
    @Singleton
    fun provideCommentRepository(commentService: FirebaseCommentService): CommentRepository {
        return CommentRepositoryImpl(commentService)
    }
}
