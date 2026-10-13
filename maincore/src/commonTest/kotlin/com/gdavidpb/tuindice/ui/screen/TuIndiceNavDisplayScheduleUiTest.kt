package com.gdavidpb.tuindice.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceNavigator
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.record.presentation.viewmodel.ScheduleViewModel
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testing.SCHEDULE_SHEET_MARKED_ATTEMPT_ID
import com.gdavidpb.tuindice.testing.ScheduleSheetTestMonday
import com.gdavidpb.tuindice.testing.createScheduleViewModel
import com.gdavidpb.tuindice.testing.createSummaryViewModel
import com.gdavidpb.tuindice.testing.rememberTestNavigator
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlinx.datetime.LocalDate
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The host side of the Schedule sheet: the one way it is opened over the tab on screen, and what a
 * marked cell asks of the host.
 */
@OptIn(ExperimentalTestApi::class)
class TuIndiceNavDisplayScheduleUiTest {
	@Test
	fun when_theHostOpensTheSchedule_then_theSheetShowsOverTheTabOnScreen_andClosesWithBack() = runTuIndiceUiTest {
		lateinit var navigator: TuIndiceNavigator

		withScheduleKoin(withEvaluation = false) {
			setTuIndiceTestContent {
				navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

				ScheduleNavDisplay(navigator = navigator)
			}

			// The first test of a cold process pays for the whole host before Summary first appears.
			waitUntil(timeoutMillis = StateTimeoutMillis) { onAllNodesWithTagExists(SummaryUiTags.ContentContainer) }
			assertNodeVisible(SummaryUiTags.ContentContainer)
			assertNodeHidden(RecordUiTags.ScheduleSheet)

			runOnIdle { navigator.openSchedule() }

			assertNodeVisible(RecordUiTags.ScheduleSheet)
			// Over Summary, not parked in Record: the tab on screen is the one the sheet belongs to.
			runOnIdle {
				assertEquals(MainSection.SUMMARY, navigator.currentTab)
				assertEquals(RecordDestination.ScheduleDialog, navigator.currentKey)
			}

			runOnIdle { navigator.pop() }

			assertNodeHidden(RecordUiTags.ScheduleSheet)
			assertNodeVisible(SummaryUiTags.ContentContainer)
		}
	}

	@Test
	fun when_aMarkedCellIsTapped_then_theHostIsAskedForThatMonday_andCanCloseTheSheet() = runTuIndiceUiTest {
		lateinit var navigator: TuIndiceNavigator
		var asked: LocalDate? = null

		withScheduleKoin(withEvaluation = true) {
			setTuIndiceTestContent {
				navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

				ScheduleNavDisplay(
					navigator = navigator,
					onNavigateToEvaluationsWeek = { weekStart ->
						asked = weekStart
						navigator.popThenSwitchTab(navigator.currentTab)
					}
				)
			}

			runOnIdle { navigator.openSchedule() }
			assertNodeVisible(RecordUiTags.ScheduleSheet)
			waitUntil(timeoutMillis = StateTimeoutMillis) {
				onAllNodesWithTagExists(
					RecordUiTags.scheduleTableEvaluation(SCHEDULE_SHEET_MARKED_ATTEMPT_ID, ScheduleDay.Monday.code)
				)
			}

			onNodeWithTag(
				RecordUiTags.scheduleTableEvaluation(SCHEDULE_SHEET_MARKED_ATTEMPT_ID, ScheduleDay.Monday.code),
				useUnmergedTree = true
			).performClick()

			waitUntil(timeoutMillis = StateTimeoutMillis) { asked != null }
			assertEquals(ScheduleSheetTestMonday, asked)
			assertNodeHidden(RecordUiTags.ScheduleSheet)
		}
	}

	@Test
	fun when_noEvaluationFallsInTheWeek_then_theCellOffersNoJumpToEvaluations() = runTuIndiceUiTest {
		lateinit var navigator: TuIndiceNavigator
		var asked: LocalDate? = null

		withScheduleKoin(withEvaluation = false) {
			setTuIndiceTestContent {
				navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

				ScheduleNavDisplay(
					navigator = navigator,
					onNavigateToEvaluationsWeek = { weekStart -> asked = weekStart }
				)
			}

			runOnIdle { navigator.openSchedule() }
			waitUntil(timeoutMillis = StateTimeoutMillis) {
				onAllNodesWithTag(
					RecordUiTags.scheduleTableRow(SCHEDULE_SHEET_MARKED_ATTEMPT_ID),
					useUnmergedTree = true
				).fetchSemanticsNodes().isNotEmpty()
			}

			assertNodeHidden(
				tag = RecordUiTags.scheduleTableEvaluation(SCHEDULE_SHEET_MARKED_ATTEMPT_ID, ScheduleDay.Monday.code),
				useUnmergedTree = true
			)
			assertNodeVisible(RecordUiTags.ScheduleSheet)
			assertEquals(null, asked)
		}
	}

	@Composable
	private fun ScheduleNavDisplay(
		navigator: TuIndiceNavigator,
		onNavigateToEvaluationsWeek: (weekStart: LocalDate) -> Unit = {}
	) {
		TuIndiceNavDisplay(
			navigator = navigator,
			onConfirmExitClick = {},
			onNavigateToExternalResource = {},
			onRecordViewModeChangeAvailable = {},
			onRecordTermSelectionAvailable = {},
			onNavigateToEvaluationsWeek = onNavigateToEvaluationsWeek,
			showTopBarBanner = {},
			onViewStateChanged = {},
			showSnackBar = {}
		)
	}

	private fun ComposeUiTest.onAllNodesWithTagExists(tag: String): Boolean =
		onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()

	private inline fun withScheduleKoin(withEvaluation: Boolean, block: () -> Unit) {
		stopKoin()
		startKoin {
			modules(
				module {
					factory { createSummaryViewModel() }
					factory<ScheduleViewModel> { createScheduleViewModel(withEvaluation = withEvaluation) }
				}
			)
		}

		try {
			block()
		} finally {
			stopKoin()
		}
	}

	private companion object {
		const val StateTimeoutMillis = 15_000L
	}
}
