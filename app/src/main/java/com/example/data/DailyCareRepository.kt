package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dailyCareDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "senior_daily_care_prefs"
)

interface DailyCareRepository {
    val dailyRecord: Flow<DailyCareRecord>
    suspend fun toggleMedication(slot: MedicationSlot)
    suspend fun addWaterCup()
    suspend fun removeWaterCup()
    suspend fun ensureCurrentDate()
}

class DataStoreDailyCareRepository(
    private val dataStore: DataStore<Preferences>,
    private val dateKeyProvider: () -> String = { KoreanDateFormatter.todayKey() }
) : DailyCareRepository {

    override val dailyRecord: Flow<DailyCareRecord> = dataStore.data.map { prefs ->
        val today = dateKeyProvider()
        val storedDate = prefs[KEY_DATE]
        if (storedDate != today) {
            DailyCareRecord(dateKey = today)
        } else {
            DailyCareRecord(
                dateKey = today,
                morningTaken = prefs[KEY_MORNING_TAKEN] ?: false,
                lunchTaken = prefs[KEY_LUNCH_TAKEN] ?: false,
                dinnerTaken = prefs[KEY_DINNER_TAKEN] ?: false,
                waterCups = (prefs[KEY_WATER_CUPS] ?: 0).coerceAtLeast(0)
            )
        }
    }

    override suspend fun toggleMedication(slot: MedicationSlot) {
        val today = dateKeyProvider()
        dataStore.edit { prefs ->
            resetIfNewDay(prefs, today)
            val key = when (slot) {
                MedicationSlot.MORNING -> KEY_MORNING_TAKEN
                MedicationSlot.LUNCH -> KEY_LUNCH_TAKEN
                MedicationSlot.DINNER -> KEY_DINNER_TAKEN
            }
            val current = prefs[key] ?: false
            prefs[key] = !current
        }
    }

    override suspend fun addWaterCup() {
        val today = dateKeyProvider()
        dataStore.edit { prefs ->
            resetIfNewDay(prefs, today)
            val current = (prefs[KEY_WATER_CUPS] ?: 0).coerceAtLeast(0)
            prefs[KEY_WATER_CUPS] = current + 1
        }
    }

    override suspend fun removeWaterCup() {
        val today = dateKeyProvider()
        dataStore.edit { prefs ->
            resetIfNewDay(prefs, today)
            val current = (prefs[KEY_WATER_CUPS] ?: 0).coerceAtLeast(0)
            if (current > 0) {
                prefs[KEY_WATER_CUPS] = current - 1
            }
        }
    }

    override suspend fun ensureCurrentDate() {
        val today = dateKeyProvider()
        dataStore.edit { prefs ->
            resetIfNewDay(prefs, today)
        }
    }

    private fun resetIfNewDay(
        prefs: androidx.datastore.preferences.core.MutablePreferences,
        today: String
    ) {
        if (prefs[KEY_DATE] != today) {
            prefs[KEY_DATE] = today
            prefs[KEY_MORNING_TAKEN] = false
            prefs[KEY_LUNCH_TAKEN] = false
            prefs[KEY_DINNER_TAKEN] = false
            prefs[KEY_WATER_CUPS] = 0
        }
    }

    companion object {
        private val KEY_DATE = stringPreferencesKey("record_date")
        private val KEY_MORNING_TAKEN = booleanPreferencesKey("morning_taken")
        private val KEY_LUNCH_TAKEN = booleanPreferencesKey("lunch_taken")
        private val KEY_DINNER_TAKEN = booleanPreferencesKey("dinner_taken")
        private val KEY_WATER_CUPS = intPreferencesKey("water_cups")
    }
}
