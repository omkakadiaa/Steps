package com.steps.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.steps.app.data.Stats
import com.steps.app.data.StepEntity
import com.steps.app.data.StepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class UiState(
    val stats: Stats = Stats.empty(10_000),
    val selectedDate: String = LocalDate.now().toString(),
    val selectedDay: StepEntity? = null,
    val historyMonth: YearMonth = YearMonth.now(),
    val sensorAvailable: Boolean = false,
    val ready: Boolean = false
)

class StepsViewModel(private val repository: StepRepository) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now().toString())
    private val historyMonth = MutableStateFlow(YearMonth.now())

    val uiState: StateFlow<UiState> = combine(
        repository.observeAll(),
        repository.sensorAvailable,
        selectedDate,
        historyMonth
    ) { all, sensor, sel, month ->
        val goal = repository.getGoal()
        val stats = StepRepository.computeStats(all, LocalDate.now(), goal)
        UiState(
            stats = stats,
            selectedDate = sel,
            selectedDay = all.find { it.date == sel },
            historyMonth = month,
            sensorAvailable = sensor,
            ready = true
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    fun selectDate(date: String) {
        selectedDate.value = date
        try { historyMonth.value = YearMonth.from(LocalDate.parse(date)) } catch (_: Exception) {}
    }

    fun setGoal(goal: Int) {
        viewModelScope.launch {
            repository.setGoal(goal.coerceIn(1_000, 50_000))
            repository.ensureTodayRow()
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.ensureSeeded()
            repository.ensureTodayRow()
        }
    }
}

class StepsViewModelFactory(private val repository: StepRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = StepsViewModel(repository) as T
}
