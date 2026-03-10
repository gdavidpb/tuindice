package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.evaluations.presentation.extension.toEvaluationLocalDate
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeDisabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeEnabled
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
}
