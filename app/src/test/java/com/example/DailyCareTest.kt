package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.data.DailyCareRecord
import com.example.data.DataStoreDailyCareRepository
import com.example.data.MedicationSlot
import com.example.ui.DailyCareScreen
import com.example.ui.DailyCareUiState
import com.example.ui.DailyCareViewModel
import com.example.ui.theme.SeniorCareTheme
import java.io.File
import java.util.Date
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DailyCareTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun `repository persists medication and water across restarts and resets on new day`() = runBlocking {
        val prefsFile = File(tempFolder.root, "test_care.preferences_pb")
        var currentDay = "2026-11-16"

        val dataStore = PreferenceDataStoreFactory.create(
            produceFile = { prefsFile }
        )

        val repoSession1 = DataStoreDailyCareRepository(
            dataStore = dataStore,
            dateKeyProvider = { currentDay }
        )

        repoSession1.ensureCurrentDate()
        repoSession1.toggleMedication(MedicationSlot.MORNING)
        repoSession1.toggleMedication(MedicationSlot.DINNER)
        repoSession1.addWaterCup()
        repoSession1.addWaterCup()
        repoSession1.addWaterCup()

        // Simulate reading again on the same day (app restart)
        val repoSession2 = DataStoreDailyCareRepository(
            dataStore = dataStore,
            dateKeyProvider = { currentDay }
        )
        val sameDayRecord = repoSession2.dailyRecord.first()
        assertEquals("2026-11-16", sameDayRecord.dateKey)
        assertTrue(sameDayRecord.morningTaken)
        assertFalse(sameDayRecord.lunchTaken)
        assertTrue(sameDayRecord.dinnerTaken)
        assertEquals(3, sameDayRecord.waterCups)

        // Advance to next day and verify automatic reset
        currentDay = "2026-11-17"
        val nextDayRecord = repoSession2.dailyRecord.first()
        assertEquals("2026-11-17", nextDayRecord.dateKey)
        assertFalse(nextDayRecord.morningTaken)
        assertFalse(nextDayRecord.lunchTaken)
        assertFalse(nextDayRecord.dinnerTaken)
        assertEquals(0, nextDayRecord.waterCups)
    }

    @Test
    fun `ui toggles medication completion and increments water count`() {
        composeTestRule.setContent {
            val state = androidx.compose.runtime.remember {
                androidx.compose.runtime.mutableStateOf(
                    DailyCareUiState(
                        formattedDate = "2026년 11월 16일",
                        dayOfWeek = "월요일",
                        record = DailyCareRecord(dateKey = "2026-11-16")
                    )
                )
            }
            SeniorCareTheme {
                DailyCareScreen(
                    uiState = state.value,
                    onToggleMedication = { slot ->
                        val r = state.value.record
                        state.value = state.value.copy(
                            record = when (slot) {
                                MedicationSlot.MORNING -> r.copy(morningTaken = !r.morningTaken)
                                MedicationSlot.LUNCH -> r.copy(lunchTaken = !r.lunchTaken)
                                MedicationSlot.DINNER -> r.copy(dinnerTaken = !r.dinnerTaken)
                            }
                        )
                    },
                    onAddWaterCup = {
                        state.value = state.value.copy(
                            record = state.value.record.copy(
                                waterCups = state.value.record.waterCups + 1
                            )
                        )
                    },
                    onRemoveWaterCup = {
                        state.value = state.value.copy(
                            record = state.value.record.copy(
                                waterCups = (state.value.record.waterCups - 1).coerceAtLeast(0)
                            )
                        )
                    }
                )
            }
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("today_date_text")
            .assertTextEquals("2026년 11월 16일 월요일")

        composeTestRule.onNodeWithTag("med_button_morning").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("복용 완료").assertIsDisplayed()

        composeTestRule.onNodeWithTag("add_water_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("water_count_number").assertTextEquals("1")

        composeTestRule.onNodeWithTag("add_water_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("water_count_number").assertTextEquals("2")

        composeTestRule.onNodeWithTag("remove_water_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("water_count_number").assertTextEquals("1")
    }

    @Test
    fun `millisUntilNextMidnight computes remaining milliseconds until 00_00_00_050`() {
        val cal = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.NOVEMBER, 16, 23, 59, 50)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val delayMs = com.example.data.KoreanDateFormatter.millisUntilNextMidnight(cal.time)
        assertEquals(10_050L, delayMs)
    }

    @Test
    fun `viewmodel automatically resets medication and water after midnight ticker fires`() = runBlocking {
        val prefsFile = File(tempFolder.root, "midnight_vm.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(produceFile = { prefsFile })

        val beforeMidnight = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.NOVEMBER, 16, 23, 59, 58)
            set(java.util.Calendar.MILLISECOND, 0)
        }.time
        val afterMidnight = java.util.Calendar.getInstance().apply {
            set(2026, java.util.Calendar.NOVEMBER, 17, 0, 0, 1)
            set(java.util.Calendar.MILLISECOND, 0)
        }.time

        var currentNow = beforeMidnight
        val repository = DataStoreDailyCareRepository(
            dataStore = dataStore,
            dateKeyProvider = { com.example.data.KoreanDateFormatter.todayKey(currentNow) }
        )

        val viewModel = DailyCareViewModel(
            repository = repository,
            nowProvider = { currentNow },
            midnightDelayProvider = { 150L }
        )

        val collectorJob = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Unconfined).launch {
            viewModel.uiState.collect { }
        }

        try {
            viewModel.onToggleMedication(MedicationSlot.MORNING)
            viewModel.onToggleMedication(MedicationSlot.LUNCH)
            viewModel.onAddWaterCup()
            viewModel.onAddWaterCup()

            // Wait for writes before midnight
            kotlinx.coroutines.withTimeout(3_000L) {
                while (viewModel.uiState.value.record.waterCups != 2 ||
                    !viewModel.uiState.value.record.morningTaken
                ) {
                    org.robolectric.shadows.ShadowLooper.idleMainLooper(
                        50L,
                        java.util.concurrent.TimeUnit.MILLISECONDS
                    )
                    kotlinx.coroutines.delay(25L)
                }
            }

            // Simulate midnight passing
            currentNow = afterMidnight

            kotlinx.coroutines.withTimeout(3_000L) {
                while (viewModel.uiState.value.record.dateKey != "2026-11-17" ||
                    viewModel.uiState.value.record.waterCups != 0 ||
                    viewModel.uiState.value.record.morningTaken ||
                    viewModel.uiState.value.record.lunchTaken
                ) {
                    org.robolectric.shadows.ShadowLooper.idleMainLooper(
                        100L,
                        java.util.concurrent.TimeUnit.MILLISECONDS
                    )
                    kotlinx.coroutines.delay(25L)
                }
            }

            val resetState = viewModel.uiState.value
            assertEquals("2026년 11월 17일", resetState.formattedDate)
            assertEquals("화요일", resetState.dayOfWeek)
            assertFalse(resetState.record.morningTaken)
            assertFalse(resetState.record.lunchTaken)
            assertFalse(resetState.record.dinnerTaken)
            assertEquals(0, resetState.record.waterCups)
        } finally {
            collectorJob.cancel()
        }
    }
}
