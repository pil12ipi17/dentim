package com.dentim.karaoke.data.remote.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * OkHttp interceptor for logging network requests and responses
 * Provides detailed logging for debugging purposes
 */
class LoggingInterceptor(
    private val isDebug: Boolean = true
) : Interceptor {
    
    companion object {
        private const val TAG = "KaraokeAPI"
    }
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        
        if (!isDebug) {
            return chain.proceed(request)
        }
        
        // Log request
        Log.d(TAG, "Request: ${request.method} ${request.url}")
        Log.d(TAG, "Request Headers: ${request.headers}")
        
        val startTime = System.currentTimeMillis()
        val response = chain.proceed(request)
        val endTime = System.currentTimeMillis()
        
        // Log response
        Log.d(TAG, "Response: ${response.code} ${response.message}")
        Log.d(TAG, "Response Time: ${endTime - startTime}ms")
        Log.d(TAG, "Response Headers: ${response.headers}")
        
        // Log response body (for debugging only, can be memory intensive)
        if (response.body?.contentLength() ?: 0 < 1024 * 1024) { // Less than 1MB
            val responseBody = response.peekBody(1024 * 1024) // Peek up to 1MB
            Log.d(TAG, "Response Body: ${responseBody.string()}")
        }
        
        return response
    }
}