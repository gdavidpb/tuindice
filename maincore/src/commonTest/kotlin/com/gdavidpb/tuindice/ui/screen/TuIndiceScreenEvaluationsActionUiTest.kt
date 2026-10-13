package com.gdavidpb.tuindice.ui.screen

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.model.MainShellState
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.testing.createSummaryViewModel
import com.gdavidpb.tuindice.testing.rememberTestNavigator
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertContentEquals

/**
 * The bar's "Ver horario" action of the Evaluations tab: it is offered only when the tab says the
 * current term has a schedule, with the words of the same action in Record, and it reaches the host.
 */
@OptIn(ExperimentalTestApi::class)
class TuIndiceScreenEvaluationsActionUiTest {
	@Test
	fun when_evaluationsHasASchedule_then_theBarOffersTheScheduleWithTheSameWordsAsRecords() = runTuIndiceUiTest {
		withScreenKoin {
			setTuIndiceTestContent {
				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = SummaryDestination.Summary
					),
					shellState = MainShellState(
						topBarTitle = UiText.Raw("Evaluaciones"),
						topBarConfig = TopBarConfig.EvaluationsWithSchedule,
						isTopBarVisible = true
					),
					onRetryStartUp = {},
					onUpdateAppClick = {},
					navigator = rememberTestNavigator(startKey = SummaryDestination.Summary),
					snackbarHostState = remember { SnackbarHostState() },
					onAction = {},
					onRecordViewModeChange = null,
					onRecordViewModeChangeAvailable = {},
					onNavigateTo = {},
					onNavigateBack = {},
					onConfirmExitClick = {},
					onNavigateToExternalResource = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			assertNodeVisible(
				tag = BaseUiTags.topBarActionButton(TopBarAction.EvaluationsScheduleAction),
				useUnmergedTree = true
			)
			onNodeWithContentDescription("Ver horario", useUnmergedTree = true).assertExists()
		}
	}

	@Test
	fun when_evaluationsHasNoSchedule_then_theBarOffersNoScheduleAction() = runTuIndiceUiTest {
		withScreenKoin {
			setTuIndiceTestContent {
				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = SummaryDestination.Summary
					),
					shellState = MainShellState(
						topBarTitle = UiText.Raw("Evaluaciones"),
						topBarConfig = null,
						isTopBarVisible = true
					),
					onRetryStartUp = {},
					onUpdateAppClick = {},
					navigator = rememberTestNavigator(startKey = SummaryDestination.Summary),
					snackbarHostState = remember { SnackbarHostState() },
					onAction = {},
					onRecordViewModeChange = null,
					onRecordViewModeChangeAvailable = {},
					onNavigateTo = {},
					onNavigateBack = {},
					onConfirmExitClick = {},
					onNavigateToExternalResource = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			assertNodeHidden(
				tag = BaseUiTags.topBarActionButton(TopBarAction.EvaluationsScheduleAction),
				useUnmergedTree = true
			)
		}
	}

	@Test
	fun when_theScheduleActionIsTapped_then_invokesOnActionCallback() = runTuIndiceUiTest {
		val actions = mutableListOf<TopBarAction>()

		withScreenKoin {
			setTuIndiceTestContent {
				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = SummaryDestination.Summary
					),
					shellState = MainShellState(
						topBarTitle = UiText.Raw("Evaluaciones"),
						topBarConfig = TopBarConfig.EvaluationsWithSchedule,
						isTopBarVisible = true
					),
					onRetryStartUp = {},
					onUpdateAppClick = {},
					navigator = rememberTestNavigator(startKey = SummaryDestination.Summary),
					snackbarHostState = remember { SnackbarHostState() },
					onAction = { action -> actions += action },
					onRecordViewModeChange = null,
					onRecordViewModeChangeAvailable = {},
					onNavigateTo = {},
					onNavigateBack = {},
					onConfirmExitClick = {},
					onNavigateToExternalResource = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			onNodeWithTag(
				BaseUiTags.topBarActionButton(TopBarAction.EvaluationsScheduleAction),
				useUnmergedTree = true
			).performClick()

			assertContentEquals(
				expected = listOf(TopBarAction.EvaluationsScheduleAction),
				actual = actions
			)
		}
	}

	private inline fun withScreenKoin(block: () -> Unit) {
		stopKoin()
		startKoin {
			modules(
				module {
					factory { createSummaryViewModel() }
				}
			)
		}

		try {
			block()
		} finally {
			stopKoin()
		}
	}
}
