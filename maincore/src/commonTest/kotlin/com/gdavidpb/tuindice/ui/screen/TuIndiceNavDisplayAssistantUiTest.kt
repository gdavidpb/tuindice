package com.gdavidpb.tuindice.ui.screen

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.model.MainShellState
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.model.RecordTopBarViewModeState
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The assistant's place in the header of the tabs: it leads the bar when the screen declares a context and the
 * assistant is on, it is absent when the assistant is off, and tapping it asks the host to open the chat.
 */
@OptIn(ExperimentalTestApi::class)
class TuIndiceNavDisplayAssistantUiTest {

	@Test
	fun when_theScreenDeclaresAContextAndTheAssistantIsOn_then_theBarLeadsWithTheAssistant() = runTuIndiceUiTest {
		withScreenKoin {
			setTuIndiceTestContent {
				ScreenWith(shellState = pensumShellState(isAssistantVisible = true))
			}

			assertNodeVisible(tag = ASSISTANT_TAG, useUnmergedTree = true)
			onNodeWithContentDescription(ASSISTANT_DESCRIPTION, useUnmergedTree = true).assertExists()

			val assistantLeft = onNodeWithTag(ASSISTANT_TAG, useUnmergedTree = true)
				.fetchSemanticsNode().boundsInRoot.left
			val searchLeft = onNodeWithTag(
				testTag = BaseUiTags.topBarActionButton(TopBarAction.SearchPensumAction),
				useUnmergedTree = true
			).fetchSemanticsNode().boundsInRoot.left
			assertTrue(assistantLeft < searchLeft)
		}
	}

	@Test
	fun when_theAssistantIsOff_then_theBarOffersNoAssistant() = runTuIndiceUiTest {
		withScreenKoin {
			setTuIndiceTestContent {
				ScreenWith(shellState = pensumShellState(isAssistantVisible = false))
			}

			assertNodeHidden(tag = ASSISTANT_TAG, useUnmergedTree = true)
			assertNodeVisible(
				tag = BaseUiTags.topBarActionButton(TopBarAction.SearchPensumAction),
				useUnmergedTree = true
			)
		}
	}

	@Test
	fun when_theAssistantIsTapped_then_theHostIsAskedToOpenIt() = runTuIndiceUiTest {
		val actions = mutableListOf<TopBarAction>()

		withScreenKoin {
			setTuIndiceTestContent {
				ScreenWith(
					shellState = pensumShellState(isAssistantVisible = true),
					onAction = { action -> actions += action }
				)
			}

			onNodeWithTag(ASSISTANT_TAG, useUnmergedTree = true).performClick()

			runOnIdle {
				assertEquals(listOf<TopBarAction>(TopBarAction.AssistantAction), actions)
			}
		}
	}

	@Test
	fun when_recordHasAScheduleAndTheAssistant_then_theOverflowKeepsTheTermAndTheSchedule() = runTuIndiceUiTest {
		withScreenKoin {
			setTuIndiceTestContent {
				ScreenWith(
					shellState = MainShellState(
						topBarTitle = UiText.Raw("Registro"),
						topBarConfig = TopBarConfig.RecordWithSchedule,
						isTopBarVisible = true,
						recordTopBarViewModeState = RecordTopBarViewModeState(
							selectedMode = RecordViewMode.Projection
						),
						assistantContext = AssistantScreenContext(screen = AssistantScreen.RECORD),
						isAssistantVisible = true
					),
					onRecordViewModeChange = { }
				)
			}

			assertNodeVisible(tag = ASSISTANT_TAG, useUnmergedTree = true)
			assertNodeVisible(tag = BaseUiTags.TopAppBarOverflowButton, useUnmergedTree = true)
			assertNodeHidden(
				tag = BaseUiTags.topBarActionButton(TopBarAction.RecordTermSelectionAction),
				useUnmergedTree = true
			)
			assertNodeHidden(
				tag = BaseUiTags.topBarActionButton(TopBarAction.RecordScheduleAction),
				useUnmergedTree = true
			)
		}
	}

	@Composable
	private fun ScreenWith(
		shellState: MainShellState,
		onAction: (TopBarAction) -> Unit = {},
		onRecordViewModeChange: ((RecordViewMode) -> Unit)? = null
	) {
		TuIndiceScreen(
			state = Main.State.Content(
				startDestination = SummaryDestination.Summary
			),
			shellState = shellState,
			onRetryStartUp = {},
			onUpdateAppClick = {},
			navigator = rememberTestNavigator(startKey = SummaryDestination.Summary),
			snackbarHostState = remember { SnackbarHostState() },
			onAction = onAction,
			onRecordViewModeChange = onRecordViewModeChange,
			onRecordViewModeChangeAvailable = {},
			onNavigateTo = {},
			onNavigateBack = {},
			onConfirmExitClick = {},
			onNavigateToExternalResource = {},
			onViewStateChanged = {},
			showSnackBar = {}
		)
	}

	private fun pensumShellState(isAssistantVisible: Boolean) = MainShellState(
		topBarTitle = UiText.Raw("Pensum"),
		topBarConfig = TopBarConfig.Pensum,
		isTopBarVisible = true,
		assistantContext = AssistantScreenContext(screen = AssistantScreen.PENSUM),
		isAssistantVisible = isAssistantVisible
	)

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

	private companion object {
		// The tag and the words of the assistant's button, as a screen reader and a test see them.
		val ASSISTANT_TAG = BaseUiTags.topBarActionButton(TopBarAction.AssistantAction)
		const val ASSISTANT_DESCRIPTION = "Pregúntale a TuIndice"
	}
}
