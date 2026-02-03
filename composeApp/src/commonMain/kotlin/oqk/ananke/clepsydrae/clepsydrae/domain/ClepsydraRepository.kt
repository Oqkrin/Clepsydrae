package oqk.ananke.clepsydrae.clepsydrae.domain

import kotlinx.coroutines.flow.Flow

interface ClepsydraRepository {
    suspend fun insertClepsydra(clepsydra: Clepsydra): Long
    suspend fun getAllClepsydrae(): List<Clepsydra>
    suspend fun getClepsydraById(id: Long): Clepsydra?
    suspend fun updateClepsydra(clepsydra: Clepsydra)
    suspend fun deleteClepsydra(id: Long)
}