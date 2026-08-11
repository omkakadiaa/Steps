package com.steps.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "steps")
data class StepEntity(
    @PrimaryKey val date: String,
    val steps: Int,
    val isDemo: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
