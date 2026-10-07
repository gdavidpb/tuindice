@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceClock
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationLocalDate
import com.gdavidpb.tuindice.evaluations.testing.fixedClock
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTestApi::class)
class EvaluationDatePickerUiTest {
	@Test
	fun when_noDateTapped_then_emitsNullDate() = runTuIndiceUiTest {
		var selectedDate: Long? = 1_900_000_000_000L

		setTuIndiceTestContent {
			EvaluationDatePicker(
				selectedScheduleMode = EvaluationScheduleMode.DATED,
				selectedDate = selectedDate,
				onDateChange = { date -> selectedDate = date }
			)
		}

		assertNodeVisible(EvaluationsUiTags.EvaluationDatePicker)
		assertNodeVisible(EvaluationsUiTags.EvaluationDateSelectButton)
		assertNodeVisible(EvaluationsUiTags.EvaluationDateNoDateButton)

		onNodeWithTag(EvaluationsUiTags.EvaluationDateNoDateButton).performClick()

		assertEquals(null, selectedDate)
	}

	@Test
	fun when_dateIsCommitted_then_selectButtonReadsTheCapitalizedShortWeekdayAndTheDate() = runTuIndiceUiTest {
		val selectedDate = mutableStateOf<Long?>(LocalDate(2026, 1, 15).toEvaluationEpochMillis())

		setTuIndiceTestContent {
			EvaluationDatePicker(
				selectedScheduleMode = EvaluationScheduleMode.DATED,
				selectedDate = selectedDate.value,
				onDateChange = {}
			)
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationDateSelectButton).assertTextEquals("Jue — 15/01/26")

		runOnIdle {
			selectedDate.value = null
		}

		// Without a date the button asks for one instead.
		onNodeWithTag(EvaluationsUiTags.EvaluationDateSelectButton).assertTextEquals("Elige una fecha")
	}

	@Test
	fun when_selectDateTapped_then_opensAndClosesDialog() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationDatePicker(
				selectedScheduleMode = EvaluationScheduleMode.DATED,
				selectedDate = 1_900_000_000_000L,
				onDateChange = {}
			)
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationDateSelectButton).performClick()

		assertNodeVisible(EvaluationsUiTags.EvaluationDateDialogTitle)
		assertNodeVisible(EvaluationsUiTags.EvaluationDateDialogCancelButton)

		onNodeWithTag(EvaluationsUiTags.EvaluationDateDialogCancelButton).performClick()
	}

	@Test
	fun when_dateIsSelectedInDialog_then_acceptIsEnabledAndDispatchesDate() = runTuIndiceUiTest {
		var selectedDate: Long? = null

		setTuIndiceTestContent {
			EvaluationDatePicker(
				selectedScheduleMode = EvaluationScheduleMode.CONTINUOUS,
				selectedDate = null,
				onDateChange = { date ->
					selectedDate = date
				}
			)
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationDateSelectButton).performClick()

		assertNodeVisible(EvaluationsUiTags.EvaluationDateDialogTitle)
		assertNodeDisabled(EvaluationsUiTags.EvaluationDateDialogAcceptButton)

		onNodeWithTag(EvaluationsUiTags.calendarDayCell(15)).performClick()
		assertNodeEnabled(EvaluationsUiTags.EvaluationDateDialogAcceptButton)

		onNodeWithTag(EvaluationsUiTags.EvaluationDateDialogAcceptButton).performClick()

		assertNotNull(selectedDate)
	}

	@Test
	fun when_dateSelectionIsCanceled_then_doesNotDispatchDateAndResetsDraftSelection() = runTuIndiceUiTest {
		var dateChangeCalls = 0
		var selectedDate: Long? = null

		setTuIndiceTestContent {
			EvaluationDatePicker(
				selectedScheduleMode = EvaluationScheduleMode.CONTINUOUS,
				selectedDate = null,
				onDateChange = { date ->
					dateChangeCalls++
					selectedDate = date
				}
			)
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationDateSelectButton).performClick()
		onNodeWithTag(EvaluationsUiTags.calendarDayCell(15)).performClick()
		assertNodeEnabled(EvaluationsUiTags.EvaluationDateDialogAcceptButton)
		onNodeWithTag(EvaluationsUiTags.EvaluationDateDialogCancelButton).performClick()

		assertEquals(0, dateChangeCalls)
		assertEquals(null, selectedDate)

		onNodeWithTag(EvaluationsUiTags.EvaluationDateSelectButton).performClick()
		assertNodeDisabled(EvaluationsUiTags.EvaluationDateDialogAcceptButton)
	}

	@Test
	fun when_committedDateExistsAndAcceptTappedWithoutChanges_then_dispatchesCommittedDate() = runTuIndiceUiTest {
		val committedDate = 1_900_000_000_000L
		var selectedDate: Long? = null

		setTuIndiceTestContent {
			EvaluationDatePicker(
				selectedScheduleMode = EvaluationScheduleMode.DATED,
				selectedDate = committedDate,
				onDateChange = { date ->
					selectedDate = date
				}
			)
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationDateSelectButton).performClick()
		assertNodeEnabled(EvaluationsUiTags.EvaluationDateDialogAcceptButton)
		onNodeWithTag(EvaluationsUiTags.EvaluationDateDialogAcceptButton).performClick()

		val emittedDate = assertNotNull(selectedDate)
		assertEquals(
			committedDate.toEvaluationLocalDate(),
			emittedDate.toEvaluationLocalDate()
		)
	}

	// Without a committed date the dialog opens on the month of "today", and today is what the clock says.
	@Test
	fun when_noDateIsCommitted_then_dialogOpensOnTheMonthOfTheClockOfTheTree() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			CompositionLocalProvider(LocalTuIndiceClock provides fixedClock("2026-10-15T12:00:00Z")) {
				EvaluationDatePicker(
					selectedScheduleMode = EvaluationScheduleMode.DATED,
					selectedDate = null,
					onDateChange = {}
				)
			}
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationDateSelectButton).performClick()

		onNodeWithTag(EvaluationsUiTags.EvaluationCalendarMonthLabel).assertTextEquals("octubre 2026")
	}
}
