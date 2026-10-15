package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "prediction_records",
    indices = [
        Index(value = ["gameType"]),
        Index(value = ["roundId"]),
        Index(value = ["timestamp"])
    ]
)
data class PredictionRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val gameType: String,
    val roundId: String,
    val colorResult: String?,
    val numberResult: Int?,
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
