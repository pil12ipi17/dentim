package com.dentim.karaoke.data.remote.websocket

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton
import com.squareup.moshi.Moshi
import com.squareup.moshi.JsonAdapter
import com.dentim.karaoke.data.remote.dto.WebSocketMessageDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * WebSocket client for real-time processing progress updates
 * Handles connection management, reconnection logic, and message parsing
 */
@Singleton
class KaraokeWebSocketClient @Inject constructor(
    private val moshi: Moshi
) {
    
    companion object {
        private const val TAG = "KaraokeWebSocket"
        private const val RECONNECT_DELAY_MS = 3000L
        private const val MAX_RECONNECT_ATTEMPTS = 5
        private const val PING_INTERVAL_MS = 30000L
    }
    
    private var webSocketClient: WebSocketClient? = null
    private var reconnectJob: Job? = null
    private var pingJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    private val isConnected = AtomicBoolean(false)
    private val isConnecting = AtomicBoolean(false)
    private var reconnectAttempts = 0
    
    // Message adapter for JSON parsing
    private val messageAdapter: JsonAdapter<WebSocketMessageDto> = moshi.adapter(WebSocketMessageDto::class.java)
    
    // Flow for incoming messages
    private val _messageFlow = MutableSharedFlow<WebSocketMessageDto>()
    val messageFlow: SharedFlow<WebSocketMessageDto> = _messageFlow.asSharedFlow()
    
    // Flow for connection state
    private val _connectionStateFlow = MutableSharedFlow<ConnectionState>()
    val connectionStateFlow: SharedFlow<ConnectionState> = _connectionStateFlow.asSharedFlow()
    
    /**
     * Connect to WebSocket server
     */
    fun connect(serverUrl: String) {
        if (isConnecting.get() || isConnected.get()) {
            Log.d(TAG, "WebSocket already connecting or connected")
            return
        }
        
        isConnecting.set(true)
        
        try {
            val uri = URI(serverUrl)
            webSocketClient = object : WebSocketClient(uri) {
                override fun onOpen(handshake: ServerHandshake?) {
                    Log.d(TAG, "WebSocket connected")
                    isConnected.set(true)
                    isConnecting.set(false)
                    reconnectAttempts = 0
                    
                    scope.launch {
                        _connectionStateFlow.emit(ConnectionState.CONNECTED)
                        startPingPong()
                    }
                }
                
                override fun onMessage(message: String?) {
                    Log.d(TAG, "WebSocket message received: $message")
                    message?.let { parseAndEmitMessage(it) }
                }
                
                override fun onClose(code: Int, reason: String?, remote: Boolean) {
                    Log.d(TAG, "WebSocket closed: $code, $reason, remote: $remote")
                    isConnected.set(false)
                    isConnecting.set(false)
                    stopPingPong()
                    
                    scope.launch {
                        _connectionStateFlow.emit(ConnectionState.DISCONNECTED)
                        if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                            scheduleReconnect(serverUrl)
                        } else {
                            _connectionStateFlow.emit(ConnectionState.FAILED)
                        }
                    }
                }
                
                override fun onError(ex: Exception?) {
                    Log.e(TAG, "WebSocket error", ex)
                    scope.launch {
                        _connectionStateFlow.emit(ConnectionState.ERROR)
                    }
                }
            }
            
            webSocketClient?.connect()
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create WebSocket connection", e)
            isConnecting.set(false)
            scope.launch {
                _connectionStateFlow.emit(ConnectionState.ERROR)
            }
        }
    }
    
    /**
     * Disconnect from WebSocket server
     */
    fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket")
        
        reconnectJob?.cancel()
        stopPingPong()
        
        webSocketClient?.close()
        webSocketClient = null
        
        isConnected.set(false)
        isConnecting.set(false)
        reconnectAttempts = 0
        
        scope.launch {
            _connectionStateFlow.emit(ConnectionState.DISCONNECTED)
        }
    }
    
    /**
     * Send message to server
     */
    fun sendMessage(message: WebSocketMessageDto) {
        if (!isConnected.get()) {
            Log.w(TAG, "WebSocket not connected, cannot send message")
            return
        }
        
        try {
            val json = messageAdapter.toJson(message)
            webSocketClient?.send(json)
            Log.d(TAG, "WebSocket message sent: $json")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to send WebSocket message", e)
        }
    }
    
    /**
     * Subscribe to processing updates for a specific job
     */
    fun subscribeToProcessingUpdates(jobId: String) {
        val subscribeMessage = WebSocketMessageDto(
            type = "subscribe",
            data = mapOf("job_id" to jobId)
        )
        sendMessage(subscribeMessage)
    }
    
    /**
     * Unsubscribe from processing updates for a specific job
     */
    fun unsubscribeFromProcessingUpdates(jobId: String) {
        val unsubscribeMessage = WebSocketMessageDto(
            type = "unsubscribe", 
            data = mapOf("job_id" to jobId)
        )
        sendMessage(unsubscribeMessage)
    }
    
    private fun parseAndEmitMessage(json: String) {
        try {
            val message = messageAdapter.fromJson(json)
            message?.let { 
                scope.launch {
                    _messageFlow.emit(it)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse WebSocket message: $json", e)
        }
    }
    
    private fun scheduleReconnect(serverUrl: String) {
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            reconnectAttempts++
            Log.d(TAG, "Scheduling reconnect attempt $reconnectAttempts in ${RECONNECT_DELAY_MS}ms")
            
            _connectionStateFlow.emit(ConnectionState.RECONNECTING)
            delay(RECONNECT_DELAY_MS)
            
            if (reconnectAttempts <= MAX_RECONNECT_ATTEMPTS) {
                connect(serverUrl)
            }
        }
    }
    
    private fun startPingPong() {
        pingJob = scope.launch {
            while (isConnected.get()) {
                delay(PING_INTERVAL_MS)
                if (isConnected.get()) {
                    val pingMessage = WebSocketMessageDto(type = "ping")
                    sendMessage(pingMessage)
                }
            }
        }
    }
    
    private fun stopPingPong() {
        pingJob?.cancel()
        pingJob = null
    }
    
    /**
     * Connection state enumeration
     */
    enum class ConnectionState {
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        RECONNECTING,
        ERROR,
        FAILED
    }
}