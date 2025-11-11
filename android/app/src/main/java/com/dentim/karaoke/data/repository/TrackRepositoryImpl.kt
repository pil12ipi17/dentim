package com.dentim.karaoke.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.dentim.karaoke.data.local.dao.TrackDao
import com.dentim.karaoke.data.mapper.toDomain
import com.dentim.karaoke.data.mapper.toEntity
import com.dentim.karaoke.domain.model.Track
import com.dentim.karaoke.domain.repository.TrackRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of TrackRepository
 * Manages track data from local database
 */
@Singleton
class TrackRepositoryImpl @Inject constructor(
    private val trackDao: TrackDao
) : TrackRepository {
    
    override fun getAllTracks(): Flow<List<Track>> {
        return trackDao.getAllTracks().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getTrackById(trackId: String): Track? {
        return trackDao.getTrackById(trackId)?.toDomain()
    }
    
    override fun getTrackByIdFlow(trackId: String): Flow<Track?> {
        return trackDao.getTrackByIdFlow(trackId).map { entity ->
            entity?.toDomain()
        }
    }
    
    override suspend fun getTrackByChecksum(checksum: String): Track? {
        return trackDao.getTrackByChecksum(checksum)?.toDomain()
    }
    
    override suspend fun insertTrack(track: Track) {
        trackDao.insertTrack(track.toEntity())
    }
    
    override suspend fun updateTrack(track: Track) {
        trackDao.updateTrack(track.toEntity())
    }
    
    override suspend fun deleteTrack(trackId: String) {
        trackDao.deleteTrackById(trackId)
    }
    
    override suspend fun deleteAllTracks() {
        trackDao.deleteAllTracks()
    }
    
    override fun searchTracks(query: String): Flow<List<Track>> {
        return trackDao.searchTracks("%$query%").map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getTrackCount(): Int {
        return trackDao.getTrackCount()
    }
    
    override suspend fun getTotalFileSize(): Long {
        return trackDao.getTotalFileSize()
    }
}