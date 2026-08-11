package com.steps.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {
    @Query("SELECT * FROM steps ORDER BY date ASC")
    fun observeAll(): Flow<List<StepEntity>>

    @Query("SELECT * FROM steps ORDER BY date ASC")
    suspend fun getAll(): List<StepEntity>

    @Query("SELECT * FROM steps WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): StepEntity?

    @Query("SELECT COUNT(*) FROM steps")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(entity: StepEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(entities: List<StepEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: StepEntity)
}
