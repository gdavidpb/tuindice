package com.gdavidpb.tuindice.record.presentation.route

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicScheduleEntry
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.record.domain.model.ScheduleViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Schedule
import com.gdavidpb.tuindice.record.testing.academicAttempt
import com.gdavidpb.tuindice.record.testing.academicTerm
import com.gdavidpb.tuindice.record.testing.scheduleRouteFixture
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class ScheduleRouteUiTest {
	@Test
	fun when_theCurrentTermHasASchedule_then_theSheetOpensOnItsTable_underTheTermsName() = runTuIndiceUiTest {
		val fixture = scheduleRouteFixture(record = scheduledRecord())

		setTuIndiceTestContent {
			ScheduleRoute(viewModel = fixture.viewModel, onDismissRequest = {})
		}

		waitUntil(timeoutMillis = StateTimeoutMillis) { fixture.viewModel.state.value is Schedule.State.Content }

		assertNodeVisible(RecordUiTags.ScheduleSheet)
		onNodeWithTag(RecordUiTags.ScheduleTitle).assertTextEquals("Horario")
		onNodeWithText("Sep - Dic 2026").assertIsDisplayed()
		// The view the student left chosen is the one the sheet opens on.
		assertNodeVisible(RecordUiTags.ScheduleTable)
		assertNodeHidden(RecordUiTags.ScheduleGrid)
		onNodeWithTag(RecordUiTags.ScheduleViewTableTab).assertIsSelected()
		assertNodeVisible(RecordUiTags.scheduleTableRow("attempt-CI5311"))
	}

	@Test
	fun when_theWeekViewIsTapped_then_theChoiceIsStored_andTheSheetFollowsIt() = runTuIndiceUiTest {
		val fixture = scheduleRouteFixture(record = scheduledRecord())

		setTuIndiceTestContent {
			ScheduleRoute(viewModel = fixture.viewModel, onDismissRequest = {})
		}

		waitUntil(timeoutMillis = StateTimeoutMillis) { fixture.viewModel.state.value is Schedule.State.Content }
		assertNodeVisible(RecordUiTags.ScheduleViewWeekTab)

		onNodeWithTag(RecordUiTags.ScheduleViewWeekTab).performClick()

		waitUntil(timeoutMillis = StateTimeoutMillis) {
			(fixture.viewModel.state.value as? Schedule.State.Content)?.viewMode == ScheduleViewMode.Week
		}

		// The tap went through the view model: it is what the next opening of the sheet will read.
		assertEquals(listOf(ScheduleViewMode.Week), fixture.selectionRepository.setViewModeCalls)
		assertNodeVisible(RecordUiTags.ScheduleGrid)
		assertNodeHidden(RecordUiTags.ScheduleTable)
		onNodeWithTag(RecordUiTags.ScheduleViewWeekTab).assertIsSelected()
	}

	@Test
	fun when_theRecordIsStillToCome_then_theSheetWaits_andSaysThereIsNoScheduleOnceItArrivesEmpty() =
		runTuIndiceUiTest {
			val fixture = scheduleRouteFixture(record = AcademicRecord(id = "record"), hasSynced = false)

			setTuIndiceTestContent {
				ScheduleRoute(viewModel = fixture.viewModel, onDismissRequest = {})
			}

			waitUntil(timeoutMillis = StateTimeoutMillis) { fixture.viewModel.state.value is Schedule.State.Loading }

			assertNodeVisible(RecordUiTags.ScheduleLoadingIndicator)
			// Nothing to switch between yet.
			assertNodeHidden(RecordUiTags.ScheduleViewSwitch)

			fixture.academicRecordRepository.hasSyncedFlow.value = true

			waitUntil(timeoutMillis = StateTimeoutMillis) { fixture.viewModel.state.value is Schedule.State.Empty }
			waitForIdle()

			assertNodeHidden(RecordUiTags.ScheduleLoadingIndicator)
			onNodeWithText("Tu trimestre actual todavía no tiene horario.").assertIsDisplayed()
			assertTrue(fixture.selectionRepository.setViewModeCalls.isEmpty())
		}

	// CI5311 meets Monday (blocks 1-2) in MYS-116, in the current term.
	private fun scheduledRecord() = AcademicRecord(
		id = "record",
		terms = listOf(
			academicTerm(
				id = "now",
				kind = TermKind.CURRENT,
				periodYear = 2026,
				periodCode = AcademicTermPeriod.SEP_DEC,
				attempts = listOf(
					academicAttempt(subjectCode = "CI5311").copy(
						section = 1,
						schedule = listOf(
							AcademicScheduleEntry(dayOfWeek = 2, startBlock = 1, endBlock = 2, classroom = "MYS-116")
						)
					)
				)
			)
		)
	)

	private companion object {
		// A cold start of the view model plus the first resource load on the simulator.
		const val StateTimeoutMillis = 15_000L
	}
}
