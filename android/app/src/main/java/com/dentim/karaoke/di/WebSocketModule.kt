package com.dentim.karaoke.di

import com.dentim.karaoke.data.remote.websocket.KaraokeWebSocketClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for WebSocket dependencies
 * Provides WebSocket client for real-time communication
 */
@Module
@InstallIn(SingletonComponent::class)
object WebSocketModule {
    
    @Provides
    @Singleton
    fun provideKaraokeWebSocketClient(
        moshi: com.squareup.moshi.Moshi
    ): KaraokeWebSocketClient {
        return KaraokeWebSocketClient(moshi)
    }
}