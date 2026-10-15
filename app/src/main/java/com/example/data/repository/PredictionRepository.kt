package com.example.data.repository

import com.example.data.local.PredictionRecord
import com.example.data.local.PredictionRecordDao
import kotlinx.coroutines.flow.Flow

class PredictionRepository(private val dao: PredictionRecordDao) {

    val allRecords: Flow<List<PredictionRecord>> = dao.getAllRecords()
    val totalCount: Flow<Int> = dao.countTotal()

    fun getRecordsByGameType(gameType: String): Flow<List<PredictionRecord>> {
        return dao.getRecordsByGameType(gameType)
    }

    fun getRecentRecords(limit: Int): Flow<List<PredictionRecord>> {
        return dao.getRecentRecords(limit)
    }

    suspend fun getById(id: Long): PredictionRecord? {
        return dao.getRecordById(id)
    }

    suspend fun getByRoundId(roundId: String): PredictionRecord? {
        return dao.getByRoundId(roundId)
    }

    suspend fun insert(record: PredictionRecord): Long {
        return dao.insertRecord(record)
    }

    suspend fun insertAll(records: List<PredictionRecord>) {
        dao.insertAll(records)
    }

    suspend fun update(record: PredictionRecord) {
        dao.updateRecord(record)
    }

    suspend fun delete(record: PredictionRecord) {
        dao.deleteRecord(record)
    }

    suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }
}
