package com.dentim.karaoke.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.dentim.karaoke.data.local.dao.SessionDao
import com.dentim.karaoke.data.mapper.toDomain
import com.dentim.karaoke.data.mapper.toEntity
import com.dentim.karaoke.domain.model.Session
import com.dentim.karaoke.domain.repository.SessionRepository
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SessionRepository
 * Manages karaoke session data from local database
 */
@Singleton
class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao
) : SessionRepository {
    
    override fun getAllSessions(): Flow<List<Session>> {
        return sessionDao.getAllSessions().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getSessionById(sessionId: String): Session? {
        return sessionDao.getSessionById(sessionId)?.toDomain()
    }
    
    override fun getSessionByIdFlow(sessionId: String): Flow<Session?> {
        return sessionDao.getSessionByIdFlow(sessionId).map { entity ->
            entity?.toDomain()
        }
    }
    
    override fun getSessionsByTrackId(trackId: String): Flow<List<Session>> {
        return sessionDao.getSessionsByTrackId(trackId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getSessionsByProcessingId(processingId: String): Flow<List<Session>> {
        return sessionDao.getSessionsByProcessingId(processingId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getRecentSessions(limit: Int): Flow<List<Session>> {
        return sessionDao.getRecentSessions(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getMostPlayedSessions(limit: Int): Flow<List<Session>> {
        return sessionDao.getMostPlayedSessions(limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun insertSession(session: Session) {
        sessionDao.insertSession(session.toEntity())
    }
    
    override suspend fun updateSession(session: Session) {
        sessionDao.updateSession(session.toEntity())
    }
    
    override suspend fun updatePlaybackPosition(sessionId: String, position: Long) {
        sessionDao.updatePlaybackPosition(sessionId, position, Date())
    }
    
    override suspend fun updateVolumeLevels(sessionId: String, vocalsVolume: Float, instrumentalVolume: Float) {
        sessionDao.updateVolumeLevels(sessionId, vocalsVolume, instrumentalVolume, Date())
    }
    
    override suspend fun incrementPlayCount(sessionId: String) {
        sessionDao.incrementPlayCount(sessionId, Date(), Date())
    }
    
    override suspend fun deleteSession(sessionId: String) {
        sessionDao.deleteSessionById(sessionId)
    }
    
    override suspend fun deleteSessionsByTrackId(trackId: String) {
        sessionDao.deleteSessionsByTrackId(trackId)
    }
    
    override suspend fun deleteSessionsByProcessingId(processingId: String) {
        sessionDao.deleteSessionsByProcessingId(processingId)
    }
    
    override suspend fun getSessionCount(): Int {
        return sessionDao.getSessionCount()
    }
    
    override suspend fun getTotalPlayCount(): Int {
        return sessionDao.getTotalPlayCount()
    }
}