package com.dentim.karaoke.di

import android.content.Context
import com.dentim.karaoke.ui.player.AudioPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Dagger module for providing Audio-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {
    
    /**
     * Provides a singleton instance of AudioPlayer
     * This ensures the same player instance is used across all fragments
     * and maintains playback state during navigation
     */
    @Provides
    @Singleton
    fun provideAudioPlayer(@ApplicationContext context: Context): AudioPlayer {
        return AudioPlayer(context)
    }
}