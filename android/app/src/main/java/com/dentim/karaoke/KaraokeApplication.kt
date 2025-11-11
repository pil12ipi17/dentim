package com.dentim.karaoke

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class KaraokeApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
}