package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.PredictionRepository

interface AppContainer {
    val predictionRepository: PredictionRepository
    val userPreferencesRepository: UserPreferencesRepository
}

class DefaultAppContainer(private val application: Application) : AppContainer {
    private val database: AppDatabase by lazy {
        AppDatabase.getInstance(application)
    }

    override val predictionRepository: PredictionRepository by lazy {
        PredictionRepository(database.predictionRecordDao())
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(application)
    }
}

class ColorPredictorApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
