@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.ScheduleMeeting
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceClock
import com.gdavidpb.tuindice.base.utils.toLocalDate
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationDateCalendarItem
import com.gdavidpb.tuindice.evaluations.presentation.utils.toEvaluationEpochMillis
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
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
			committedDate.toLocalDate(),
			emittedDate.toLocalDate()
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

	@Test
	fun when_subjectHasClassOnTheChosenDay_then_dialogSaysWhereAndMarksTheClassDays() = runTuIndiceUiTest {
		// 2026-10-12 is a Monday; October 2026 opens because the committed date is in it.
		val monday = LocalDate(2026, 10, 12)

		setTuIndiceTestContent {
			EvaluationDatePicker(
				selectedScheduleMode = EvaluationScheduleMode.DATED,
				selectedDate = monday.toEvaluationEpochMillis(),
				onDateChange = {},
				calendar = EvaluationDateCalendarItem(
					subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
					meetings = listOf(
						ScheduleMeeting(
							attemptId = DEFAULT_EVALUATION_SUBJECT.id,
							subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
							section = 1,
							dayOfWeek = 2,
							startBlock = 1,
							endBlock = 2,
							classroom = "MYS-116"
						)
					)
				)
			)
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationDateSelectButton).performClick()

		assertNodeVisible(EvaluationsUiTags.EvaluationDateDialogSubjectChip)
		assertNodeVisible(EvaluationsUiTags.EvaluationDateDialogSelection)
		waitUntil(timeoutMillis = RESOURCE_TIMEOUT_MILLIS) {
			onAllNodesWithText("En clase · Bloques 1-2 · MYS-116").fetchSemanticsNodes().isNotEmpty()
		}
		onNodeWithText("Lunes — 12/10/26").assertIsDisplayed()
		onNodeWithText("7:30\u20139:30 a. m.").assertIsDisplayed()
		// Mondays of October 2026 carry the dot, the days between them do not.
		assertNodeVisible(EvaluationsUiTags.calendarDayClassDot(12))
		assertNodeVisible(EvaluationsUiTags.calendarDayClassDot(19))
		onNodeWithTag(EvaluationsUiTags.calendarDayClassDot(13)).assertDoesNotExist()
	}

	@Test
	fun when_subjectHasNoClassOnTheChosenDay_then_dialogSaysOnlyTheDate() = runTuIndiceUiTest {
		val tuesday = LocalDate(2026, 10, 13)

		setTuIndiceTestContent {
			EvaluationDatePicker(
				selectedScheduleMode = EvaluationScheduleMode.DATED,
				selectedDate = tuesday.toEvaluationEpochMillis(),
				onDateChange = {},
				calendar = EvaluationDateCalendarItem(subjectCode = DEFAULT_EVALUATION_SUBJECT.code)
			)
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationDateSelectButton).performClick()

		waitUntil(timeoutMillis = RESOURCE_TIMEOUT_MILLIS) {
			onAllNodesWithText("Martes — 13/10/26").fetchSemanticsNodes().isNotEmpty()
		}
		onNodeWithTag(EvaluationsUiTags.EvaluationDateDialogSelectionPlace).assertDoesNotExist()
	}

	@Test
	fun when_noSubjectIsChosen_then_dialogHasNoChipAndNoDots() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			EvaluationDatePicker(
				selectedScheduleMode = EvaluationScheduleMode.DATED,
				selectedDate = LocalDate(2026, 10, 12).toEvaluationEpochMillis(),
				onDateChange = {}
			)
		}

		onNodeWithTag(EvaluationsUiTags.EvaluationDateSelectButton).performClick()

		assertNodeVisible(EvaluationsUiTags.EvaluationDateDialogTitle)
		onNodeWithTag(EvaluationsUiTags.EvaluationDateDialogSubjectChip).assertDoesNotExist()
		onNodeWithTag(EvaluationsUiTags.calendarDayClassDot(12)).assertDoesNotExist()
	}

	private companion object {
		const val RESOURCE_TIMEOUT_MILLIS = 5_000L
	}
}
