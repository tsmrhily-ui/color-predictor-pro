package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PredictionRecordDao {

    @Query("SELECT * FROM prediction_records ORDER BY timestamp DESC, id DESC")
    fun getAllRecords(): Flow<List<PredictionRecord>>

    @Query("SELECT * FROM prediction_records WHERE gameType = :gameType ORDER BY timestamp DESC, id DESC")
    fun getRecordsByGameType(gameType: String): Flow<List<PredictionRecord>>

    @Query("SELECT * FROM prediction_records ORDER BY timestamp DESC, id DESC LIMIT :limit")
    fun getRecentRecords(limit: Int): Flow<List<PredictionRecord>>

    @Query("SELECT * FROM prediction_records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): PredictionRecord?

    @Query("SELECT * FROM prediction_records WHERE roundId = :roundId LIMIT 1")
    suspend fun getByRoundId(roundId: String): PredictionRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: PredictionRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<PredictionRecord>)

    @Update
    suspend fun updateRecord(record: PredictionRecord)

    @Delete
    suspend fun deleteRecord(record: PredictionRecord)

    @Query("DELETE FROM prediction_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM prediction_records")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM prediction_records")
    fun countTotal(): Flow<Int>
}
