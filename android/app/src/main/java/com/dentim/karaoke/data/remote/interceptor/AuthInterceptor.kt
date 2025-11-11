package com.dentim.karaoke.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * OkHttp interceptor for authentication
 * Adds API key or token to requests if needed
 */
class AuthInterceptor(
    private val apiKey: String? = null
) : Interceptor {
    
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Add authentication header if API key is provided
        val requestBuilder = originalRequest.newBuilder()
        
        apiKey?.let { key ->
            requestBuilder.addHeader("X-API-Key", key)
        }
        
        // Add common headers
        requestBuilder
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "Karaoke-Android/1.0")
        
        val request = requestBuilder.build()
        return chain.proceed(request)
    }
}