package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DailyCareRecord
import com.example.data.DailyCareRepository
import com.example.data.KoreanDateFormatter
import com.example.data.MedicationSlot
import java.util.Date
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DailyCareUiState(
    val formattedDate: String = KoreanDateFormatter.formatKoreanDate(),
    val dayOfWeek: String = KoreanDateFormatter.formatKoreanDayOfWeek(),
    val record: DailyCareRecord = DailyCareRecord(dateKey = KoreanDateFormatter.todayKey())
)

class DailyCareViewModel(
    private val repository: DailyCareRepository,
    private val nowProvider: () -> Date = { Date() },
    private val midnightDelayProvider: (Date) -> Long = {
        KoreanDateFormatter.millisUntilNextMidnight(it)
    }
) : ViewModel() {

    private val currentDateTrigger = MutableStateFlow(nowProvider())

    val uiState: StateFlow<DailyCareUiState> = combine(
        repository.dailyRecord,
        currentDateTrigger
    ) { record, date ->
        DailyCareUiState(
            formattedDate = KoreanDateFormatter.formatKoreanDate(date),
            dayOfWeek = KoreanDateFormatter.formatKoreanDayOfWeek(date),
            record = record
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DailyCareUiState(
            formattedDate = KoreanDateFormatter.formatKoreanDate(nowProvider()),
            dayOfWeek = KoreanDateFormatter.formatKoreanDayOfWeek(nowProvider()),
            record = DailyCareRecord(dateKey = KoreanDateFormatter.todayKey(nowProvider()))
        )
    )

    init {
        onRefreshDate()
        startMidnightResetTicker()
    }

    private fun startMidnightResetTicker() {
        viewModelScope.launch {
            while (isActive) {
                val delayMillis = midnightDelayProvider(nowProvider()).coerceAtLeast(100L)
                delay(delayMillis)
                onRefreshDate()
            }
        }
    }

    fun onToggleMedication(slot: MedicationSlot) {
        viewModelScope.launch {
            repository.toggleMedication(slot)
        }
    }

    fun onAddWaterCup() {
        viewModelScope.launch {
            repository.addWaterCup()
        }
    }

    fun onRemoveWaterCup() {
        viewModelScope.launch {
            repository.removeWaterCup()
        }
    }

    fun onRefreshDate() {
        currentDateTrigger.value = nowProvider()
        viewModelScope.launch {
            repository.ensureCurrentDate()
        }
    }

    class Factory(
        private val repository: DailyCareRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DailyCareViewModel::class.java)) {
                return DailyCareViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
