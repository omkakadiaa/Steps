package com.steps.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.steps.app.data.StepDatabase
import com.steps.app.data.StepRepository
import com.steps.app.sensor.MidnightScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class StepsApplication : Application() {
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    lateinit var repository: StepRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        val db = StepDatabase.getInstance(this)
        repository = StepRepository(db.stepDao(), this)
        appScope.launch {
            repository.ensureSeeded()
            repository.ensureTodayRow()
        }
        MidnightScheduler.schedule(this)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background step tracking"
                setShowBadge(false)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "steps_tracking"
        lateinit var instance: StepsApplication
            private set
    }
}
