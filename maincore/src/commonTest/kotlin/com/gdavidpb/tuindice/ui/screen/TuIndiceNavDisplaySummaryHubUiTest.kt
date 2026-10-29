package com.gdavidpb.tuindice.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceNavigator
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.record.presentation.viewmodel.ScheduleViewModel
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testing.HubHostWeekStart
import com.gdavidpb.tuindice.testing.createScheduleViewModel
import com.gdavidpb.tuindice.testing.createSummaryViewModel
import com.gdavidpb.tuindice.testing.createWeekOfTheHubUseCase
import com.gdavidpb.tuindice.testing.rememberTestNavigator
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
 * The host side of the lines of the Summary hub, with the hub on the screen: what the display hands
 * Summary reaches the navigator (the Schedule sheet) and the host (the week of Evaluations). The
 * Record and Pensum links change the tab, which would mount those tabs with all they need; the
 * navigator-level test of the same dependencies covers them.
 */
@OptIn(ExperimentalTestApi::class)
class TuIndiceNavDisplaySummaryHubUiTest {
	@Test
	fun when_theClassOfTheWeekIsTapped_then_theScheduleSheetOpensOverSummary() = runTuIndiceUiTest {
		lateinit var navigator: TuIndiceNavigator

		withHubKoin {
			setTuIndiceTestContent {
				navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

				HubNavDisplay(navigator = navigator)
			}

			awaitHub()

			onNodeWithTag(SummaryUiTags.WeekClassRow).performScrollTo().performClick()

			assertNodeVisible(RecordUiTags.ScheduleSheet)
			runOnIdle {
				assertEquals(MainSection.SUMMARY, navigator.currentTab)
				assertEquals(
					listOf(SummaryDestination.Summary, RecordDestination.ScheduleDialog),
					navigator.backStack.toList()
				)
			}
		}
	}

	@Test
	fun when_theScheduleLinkOfTheWeekIsTapped_then_theScheduleSheetOpensOverSummary() = runTuIndiceUiTest {
		lateinit var navigator: TuIndiceNavigator

		withHubKoin {
			setTuIndiceTestContent {
				navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

				HubNavDisplay(navigator = navigator)
			}

			awaitHub()

			onNodeWithTag(SummaryUiTags.sectionLink(SummaryUiTags.WeekSection)).performScrollTo().performClick()

			assertNodeVisible(RecordUiTags.ScheduleSheet)
			runOnIdle { assertEquals(MainSection.SUMMARY, navigator.currentTab) }
		}
	}

	@Test
	fun when_anEvaluationOfTheWeekIsTapped_then_theHostIsAskedForItsMonday_andSummaryStays() = runTuIndiceUiTest {
		lateinit var navigator: TuIndiceNavigator
		val asked = mutableListOf<LocalDate>()

		withHubKoin {
			setTuIndiceTestContent {
				navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

				HubNavDisplay(
					navigator = navigator,
					onNavigateToEvaluationsWeek = { weekStart, _ -> asked += weekStart }
				)
			}

			awaitHub()

			// Tapped twice: the line only asks, so it can be asked again while the host makes up its mind.
			onNodeWithTag(SummaryUiTags.weekEvaluationRow("friday")).performScrollTo().performClick()
			onNodeWithTag(SummaryUiTags.weekEvaluationRow("friday")).performClick()

			waitUntil(timeoutMillis = StateTimeoutMillis) { asked.size == 2 }
			assertEquals(listOf(HubHostWeekStart, HubHostWeekStart), asked)
			runOnIdle { assertEquals(MainSection.SUMMARY, navigator.currentTab) }
		}
	}

	@Composable
	private fun HubNavDisplay(
		navigator: TuIndiceNavigator,
		onNavigateToEvaluationsWeek: (weekStart: LocalDate, evaluationId: String) -> Unit = { _, _ -> }
	) {
		TuIndiceNavDisplay(
			onSignOutRequest = {},
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

	// The first test of a cold process pays for the whole host before Summary first appears.
	private fun ComposeUiTest.awaitHub() {
		waitUntil(timeoutMillis = StateTimeoutMillis) {
			onAllNodesWithTag(SummaryUiTags.WeekClassRow, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
		}
	}

	private inline fun withHubKoin(block: () -> Unit) {
		stopKoin()
		startKoin {
			modules(
				module {
					factory { createSummaryViewModel(hub = createWeekOfTheHubUseCase()) }
					factory<ScheduleViewModel> { createScheduleViewModel(withEvaluation = false) }
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
