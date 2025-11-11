package com.dentim.karaoke.di

import com.dentim.karaoke.data.repository.TrackRepositoryImpl
import com.dentim.karaoke.data.repository.ProcessingRepositoryImpl
import com.dentim.karaoke.data.repository.SessionRepositoryImpl
import com.dentim.karaoke.domain.repository.TrackRepository
import com.dentim.karaoke.domain.repository.ProcessingRepository
import com.dentim.karaoke.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for repository dependencies
 * Binds repository implementations to their interfaces
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindTrackRepository(
        trackRepositoryImpl: TrackRepositoryImpl
    ): TrackRepository
    
    @Binds
    @Singleton
    abstract fun bindProcessingRepository(
        processingRepositoryImpl: ProcessingRepositoryImpl
    ): ProcessingRepository
    
    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        sessionRepositoryImpl: SessionRepositoryImpl
    ): SessionRepository
}