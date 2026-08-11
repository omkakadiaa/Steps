package com.steps.app.data

import android.content.Context
import com.steps.app.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class StepRepository(
    private val dao: StepDao,
    private val context: Context
) {
    private val mutex = Mutex()
    private val _liveBaseline = MutableStateFlow<Int?>(null)
    private var baselineDate: String? = null
    private val _sensorAvailable = MutableStateFlow(false)
    val sensorAvailable: StateFlow<Boolean> = _sensorAvailable.asStateFlow()

    fun observeAll(): Flow<List<StepEntity>> = dao.observeAll()

    suspend fun ensureSeeded() {
        mutex.withLock {
            if (dao.count() > 0) return
            val demo = loadDemoFromAssets()
            if (demo.isNotEmpty()) dao.insertAllIgnore(demo)
        }
    }

    private fun loadDemoFromAssets(): List<StepEntity> {
        return try {
            context.assets.open("demo_steps.json").bufferedReader().use { reader ->
                val arr = JSONArray(reader.readText())
                buildList {
                    for (i in 0 until arr.length()) {
                        val o = arr.getJSONObject(i)
                        add(StepEntity(date = o.getString("date"), steps = o.getInt("steps"), isDemo = true))
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun ensureTodayRow() {
        val today = DateUtils.today()
        if (dao.getByDate(today) == null) {
            dao.insertIgnore(StepEntity(date = today, steps = 0, isDemo = false))
        }
    }

    suspend fun onSensorStepCounter(cumulativeSinceBoot: Int) {
        mutex.withLock {
            _sensorAvailable.value = true
            val today = DateUtils.today()
            if (baselineDate != today) {
                val existing = dao.getByDate(today)
                val already = when {
                    existing == null -> 0
                    existing.isDemo -> 0
                    else -> existing.steps
                }
                _liveBaseline.value = (cumulativeSinceBoot - already).coerceAtLeast(0)
                baselineDate = today
                if (existing == null) {
                    dao.insertIgnore(StepEntity(date = today, steps = 0, isDemo = false))
                }
            }
            var baseline = _liveBaseline.value ?: 0
            if (cumulativeSinceBoot < baseline) {
                val existing = dao.getByDate(today)?.steps ?: 0
                baseline = (cumulativeSinceBoot - existing).coerceAtLeast(0)
                _liveBaseline.value = baseline
            }
            val liveSteps = (cumulativeSinceBoot - baseline).coerceAtLeast(0)
            val row = dao.getByDate(today)
            val finalSteps = when {
                row != null && row.isDemo && liveSteps < row.steps -> row.steps
                else -> liveSteps
            }
            val isDemo = row != null && row.isDemo && liveSteps < row.steps
            dao.upsert(StepEntity(date = today, steps = finalSteps, isDemo = isDemo))
        }
    }

    suspend fun rollDay() {
        mutex.withLock {
            _liveBaseline.value = null
            baselineDate = null
            val today = DateUtils.today()
            if (dao.getByDate(today) == null) {
                dao.insertIgnore(StepEntity(date = today, steps = 0, isDemo = false))
            }
        }
    }

    fun markSensorAvailable(available: Boolean) { _sensorAvailable.value = available }

    suspend fun setGoal(goal: Int) {
        context.getSharedPreferences("steps_prefs", Context.MODE_PRIVATE)
            .edit().putInt("daily_goal", goal.coerceIn(1_000, 50_000)).apply()
    }

    fun getGoal(): Int =
        context.getSharedPreferences("steps_prefs", Context.MODE_PRIVATE)
            .getInt("daily_goal", 10_000)

    companion object {
        fun computeStats(all: List<StepEntity>, today: LocalDate = LocalDate.now(), goal: Int = 10_000): Stats {
            if (all.isEmpty()) return Stats.empty(goal)
            val byDate = all.associateBy { it.date }
            val todayStr = today.toString()
            val todaySteps = byDate[todayStr]?.steps ?: 0
            val weekDates = (0L..6L).map { today.minusDays(6 - it) }
            val weekPresent = weekDates.mapNotNull { byDate[it.toString()]?.steps }
            val weekAvg = if (weekPresent.isEmpty()) 0 else weekPresent.average().roundToInt()
            val monthStart = today.withDayOfMonth(1)
            val monthDays = ChronoUnit.DAYS.between(monthStart, today).toInt() + 1
            val monthValues = (0 until monthDays).mapNotNull {
                byDate[monthStart.plusDays(it.toLong()).toString()]?.steps
            }
            val monthAvg = if (monthValues.isEmpty()) 0 else monthValues.average().roundToInt()
            val yearValues = all.filter {
                val d = LocalDate.parse(it.date)
                d.year == today.year && !d.isAfter(today)
            }
            val yearAvg = if (yearValues.isEmpty()) 0 else yearValues.map { it.steps }.average().roundToInt()
            val yearTotal = yearValues.sumOf { it.steps.toLong() }
            val recent = (0L..13L).map { today.minusDays(13 - it) }.map { d ->
                DayPoint(d.toString(), byDate[d.toString()]?.steps ?: 0, byDate[d.toString()]?.isDemo ?: false)
            }
            return Stats(
                todaySteps = todaySteps,
                todayIsDemo = byDate[todayStr]?.isDemo ?: false,
                goal = goal,
                weekAvg = weekAvg,
                monthAvg = monthAvg,
                yearAvg = yearAvg,
                yearTotal = yearTotal,
                recent = recent,
                all = all.sortedBy { it.date },
                bestDay = all.maxByOrNull { it.steps },
                totalDays = all.size
            )
        }
    }
}

data class DayPoint(val date: String, val steps: Int, val isDemo: Boolean)

data class Stats(
    val todaySteps: Int,
    val todayIsDemo: Boolean,
    val goal: Int,
    val weekAvg: Int,
    val monthAvg: Int,
    val yearAvg: Int,
    val yearTotal: Long,
    val recent: List<DayPoint>,
    val all: List<StepEntity>,
    val bestDay: StepEntity?,
    val totalDays: Int
) {
    companion object {
        fun empty(goal: Int) = Stats(0, false, goal, 0, 0, 0, 0, emptyList(), emptyList(), null, 0)
    }
}
