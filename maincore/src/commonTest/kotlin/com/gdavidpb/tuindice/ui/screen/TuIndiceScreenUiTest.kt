package com.gdavidpb.tuindice.ui.screen

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.model.MainShellState
import com.gdavidpb.tuindice.presentation.navigation.BrowserDestination
import com.gdavidpb.tuindice.presentation.navigation.MainDestination
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceNavigator
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.model.RecordTopBarViewModeState
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.testing.createBrowserViewModel
import com.gdavidpb.tuindice.testing.createSummaryViewModel
import com.gdavidpb.tuindice.testing.rememberTestNavigator
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TuIndiceScreenUiTest {
	@Test
	fun when_stateIsStarting_then_displaysStartingIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			TuIndiceScreen(
				state = Main.State.Starting,
				shellState = shellState(),
				onRetryStartUp = {},
				onUpdateAppClick = {},
				navigator = null,
				snackbarHostState = remember { SnackbarHostState() },
				onAction = {},
				onRecordViewModeChange = null,
				onRecordViewModeChangeAvailable = {},
				onNavigateTo = {},
				onNavigateBack = {},
				onConfirmExitClick = {},
				isCameraAvailable = false,
				onNavigateToExternalResource = {},
				onViewStateChanged = {},
				showSnackBar = {}
			)
		}

		assertNodeVisible(MaincoreUiTags.TuIndiceStartingIndicator)
	}

	@Test
	fun when_stateIsFailedAndRetryTapped_then_invokesRetryCallback() = runTuIndiceUiTest {
		var retryCalls = 0

		setTuIndiceTestContent {
			TuIndiceScreen(
				state = Main.State.Failed,
				shellState = shellState(),
				onRetryStartUp = { retryCalls++ },
				onUpdateAppClick = {},
				navigator = null,
				snackbarHostState = remember { SnackbarHostState() },
				onAction = {},
				onRecordViewModeChange = null,
				onRecordViewModeChangeAvailable = {},
				onNavigateTo = {},
				onNavigateBack = {},
				onConfirmExitClick = {},
				isCameraAvailable = false,
				onNavigateToExternalResource = {},
				onViewStateChanged = {},
				showSnackBar = {}
			)
		}

		assertNodeVisible(BaseUiTags.ErrorStateAnimation)
		assertNodeVisible(BaseUiTags.ErrorViewRetryButton)
		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).performClick()
		assertEquals(1, retryCalls)
	}

	@Test
	fun when_stateIsAppUnavailable_then_displaysBlockingAvailabilityNotice() = runTuIndiceUiTest {
		var retryCalls = 0

		setTuIndiceTestContent {
			TuIndiceScreen(
				state = Main.State.AppUnavailable(
					notice = AppAvailabilityNotice(
						enabled = true,
						title = "Servicio pausado",
						message = "Estamos en mantenimiento."
					)
				),
				shellState = shellState(
					topBarTitle = "Resumen",
					topBarConfig = TopBarConfig.Summary,
					isTopBarVisible = true,
					isBottomBarVisible = true
				),
				onRetryStartUp = { retryCalls++ },
				onUpdateAppClick = {},
				navigator = null,
				snackbarHostState = remember { SnackbarHostState() },
				onAction = {},
				onRecordViewModeChange = null,
				onRecordViewModeChangeAvailable = {},
				onNavigateTo = {},
				onNavigateBack = {},
				onConfirmExitClick = {},
				isCameraAvailable = false,
				onNavigateToExternalResource = {},
				onViewStateChanged = {},
				showSnackBar = {}
			)
		}

		assertNodeVisible(MaincoreUiTags.AppAvailabilityNoticeScreen)
		assertNodeVisible(BaseUiTags.ErrorStateAnimation)
		onNodeWithText("Servicio pausado").assertExists()
		onNodeWithText("Estamos en mantenimiento.").assertExists()
		assertNodeHidden(MaincoreUiTags.TuIndiceNavHost)
		assertNodeHidden(MaincoreUiTags.TuIndiceBottomBar)
		assertNodeHidden(BaseUiTags.TopAppBarActionsContainer)
		assertNodeVisible(MaincoreUiTags.AppAvailabilityNoticeRetryButton)
		onNodeWithTag(MaincoreUiTags.AppAvailabilityNoticeRetryButton).performClick()
		assertEquals(1, retryCalls)
	}

	@Test
	fun when_stateIsOutdatedApp_then_displaysBlockingUpdateScreen() = runTuIndiceUiTest {
		var updateClicks = 0

		setTuIndiceTestContent {
			TuIndiceScreen(
				state = Main.State.OutdatedApp(
					outdatedAppState = OutdatedAppState(minimumVersionCode = 52)
				),
				shellState = shellState(
					topBarTitle = "Resumen",
					topBarConfig = TopBarConfig.Summary,
					isTopBarVisible = true,
					isBottomBarVisible = true
				),
				onRetryStartUp = {},
				onUpdateAppClick = { updateClicks++ },
				navigator = null,
				snackbarHostState = remember { SnackbarHostState() },
				onAction = {},
				onRecordViewModeChange = null,
				onRecordViewModeChangeAvailable = {},
				onNavigateTo = {},
				onNavigateBack = {},
				onConfirmExitClick = {},
				isCameraAvailable = false,
				onNavigateToExternalResource = {},
				onViewStateChanged = {},
				showSnackBar = {}
			)
		}

		assertNodeVisible(BaseUiTags.OutdatedAppScreen)
		assertNodeVisible(BaseUiTags.OutdatedAppAnimation)
		assertNodeHidden(MaincoreUiTags.TuIndiceNavHost)
		assertNodeHidden(MaincoreUiTags.TuIndiceBottomBar)
		assertNodeHidden(BaseUiTags.TopAppBarActionsContainer)
		onNodeWithTag(BaseUiTags.OutdatedAppUpdateButton).performClick()
		assertEquals(1, updateClicks)
	}

	@Test
	fun when_stateIsContentWithBottomBarVisible_then_displaysBottomBar() = runTuIndiceUiTest {
		withScreenKoin {
			setTuIndiceTestContent {
				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = browserStartDestination()
					),
					shellState = shellState(isBottomBarVisible = true),
					onRetryStartUp = {},
					onUpdateAppClick = {},
					navigator = rememberTestNavigator(startKey = browserStartDestination()),
					snackbarHostState = remember { SnackbarHostState() },
					onAction = {},
					onRecordViewModeChange = null,
					onRecordViewModeChangeAvailable = {},
					onNavigateTo = {},
					onNavigateBack = {},
					onConfirmExitClick = {},
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			assertNodeVisible(MaincoreUiTags.TuIndiceBottomBar)
		}
	}

	@Test
	fun when_topBarConfigIsSummary_then_displaysSignOutActionButton() = runTuIndiceUiTest {
		withScreenKoin {
			setTuIndiceTestContent {
				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = browserStartDestination()
					),
					shellState = shellState(
						topBarTitle = "Resumen",
						topBarConfig = TopBarConfig.Summary,
						isTopBarVisible = true
					),
					onRetryStartUp = {},
					onUpdateAppClick = {},
					navigator = rememberTestNavigator(startKey = browserStartDestination()),
					snackbarHostState = remember { SnackbarHostState() },
					onAction = {},
					onRecordViewModeChange = null,
					onRecordViewModeChangeAvailable = {},
					onNavigateTo = {},
					onNavigateBack = {},
					onConfirmExitClick = {},
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			assertNodeVisible(
				tag = BaseUiTags.topBarActionButton(TopBarAction.SignOutAction),
				useUnmergedTree = true
			)
		}
	}

	@Test
	fun when_recordTopBarViewModeStateIsPresent_then_displaysSwitchWithoutBannerByDefault() = runTuIndiceUiTest {
		withScreenKoin {
			setTuIndiceTestContent {
				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = browserStartDestination()
					),
					shellState = shellState(
						topBarTitle = "Record",
						isTopBarVisible = true,
						recordTopBarViewModeState = RecordTopBarViewModeState(
							selectedMode = RecordViewMode.Projection
						)
					),
					onRetryStartUp = {},
					onUpdateAppClick = {},
					navigator = rememberTestNavigator(startKey = browserStartDestination()),
					snackbarHostState = remember { SnackbarHostState() },
					onAction = {},
					onRecordViewModeChange = {},
					onRecordViewModeChangeAvailable = {},
					onNavigateTo = {},
					onNavigateBack = {},
					onConfirmExitClick = {},
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			assertNodeVisible(RecordUiTags.TopBarViewModeSwitch)
			assertNodeVisible(RecordUiTags.TopBarViewModeButton)
			assertNodeHidden(RecordUiTags.TopBarViewModeBanner)
		}
	}

	@Test
	fun when_contentHidesBars_then_topBarAndBottomBarAreNotDisplayed() = runTuIndiceUiTest {
		withScreenKoin {
			setTuIndiceTestContent {
				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = browserStartDestination()
					),
					shellState = shellState(
						topBarTitle = "Resumen",
						topBarConfig = TopBarConfig.Summary
					),
					onRetryStartUp = {},
					onUpdateAppClick = {},
					navigator = rememberTestNavigator(startKey = browserStartDestination()),
					snackbarHostState = remember { SnackbarHostState() },
					onAction = {},
					onRecordViewModeChange = null,
					onRecordViewModeChangeAvailable = {},
					onNavigateTo = {},
					onNavigateBack = {},
					onConfirmExitClick = {},
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			assertNodeHidden(MaincoreUiTags.TuIndiceBottomBar)
			assertNodeHidden(BaseUiTags.TopAppBarActionsContainer)
		}
	}

	@Test
	fun when_bottomBarIsVisible_then_exposesTaggedBottomBarItems() = runTuIndiceUiTest {
		withScreenKoin {
			setTuIndiceTestContent {
				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = browserStartDestination()
					),
					shellState = shellState(
						topBarTitle = "Inicio",
						isBottomBarVisible = true
					),
					onRetryStartUp = {},
					onUpdateAppClick = {},
					navigator = rememberTestNavigator(startKey = browserStartDestination()),
					snackbarHostState = remember { SnackbarHostState() },
					onAction = {},
					onRecordViewModeChange = null,
					onRecordViewModeChangeAvailable = {},
					onNavigateTo = {},
					onNavigateBack = {},
					onConfirmExitClick = {},
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			assertNodeVisible(MaincoreUiTags.TuIndiceBottomBarSummaryItem)
			assertNodeVisible(MaincoreUiTags.TuIndiceBottomBarRecordItem)
			assertNodeVisible(MaincoreUiTags.TuIndiceBottomBarEvaluationsItem)
			assertNodeVisible(MaincoreUiTags.TuIndiceBottomBarAboutItem)
		}
	}

	@Test
	fun when_signOutActionTapped_then_invokesOnActionCallback() = runTuIndiceUiTest {
		val actions = mutableListOf<TopBarAction>()

		withScreenKoin {
			setTuIndiceTestContent {
				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = browserStartDestination()
					),
					shellState = shellState(
						topBarTitle = "Resumen",
						topBarConfig = TopBarConfig.Summary,
						isTopBarVisible = true
					),
					onRetryStartUp = {},
					onUpdateAppClick = {},
					navigator = rememberTestNavigator(startKey = browserStartDestination()),
					snackbarHostState = remember { SnackbarHostState() },
					onAction = { action -> actions += action },
					onRecordViewModeChange = null,
					onRecordViewModeChangeAvailable = {},
					onNavigateTo = {},
					onNavigateBack = {},
					onConfirmExitClick = {},
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			onNodeWithTag(
				BaseUiTags.topBarActionButton(TopBarAction.SignOutAction),
				useUnmergedTree = true
			).performClick()

			assertContentEquals(
				expected = listOf(TopBarAction.SignOutAction),
				actual = actions
			)
		}
	}

	@Test
	fun when_recordViewModeSwitchTapped_then_invokesViewModeCallbackWithTopBarActionVisible() = runTuIndiceUiTest {
		val actions = mutableListOf<TopBarAction>()
		val selectedModes = mutableListOf<RecordViewMode>()

		withScreenKoin {
			setTuIndiceTestContent {
				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = browserStartDestination()
					),
					shellState = shellState(
						topBarTitle = "Record",
						isTopBarVisible = true,
						topBarConfig = TopBarConfig.Record,
						recordTopBarViewModeState = RecordTopBarViewModeState(
							selectedMode = RecordViewMode.Projection
						)
					),
					onRetryStartUp = {},
					onUpdateAppClick = {},
					navigator = rememberTestNavigator(startKey = browserStartDestination()),
					snackbarHostState = remember { SnackbarHostState() },
					onAction = { action -> actions += action },
					onRecordViewModeChange = { mode -> selectedModes += mode },
					onRecordViewModeChangeAvailable = {},
					onNavigateTo = {},
					onNavigateBack = {},
					onConfirmExitClick = {},
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			assertNodeVisible(
				tag = BaseUiTags.topBarActionButton(TopBarAction.RecordTermSelectionAction),
				useUnmergedTree = true
			)
			onNodeWithTag(RecordUiTags.TopBarViewModeButton).performClick()

			assertContentEquals(
				expected = listOf(RecordViewMode.Historical),
				actual = selectedModes
			)
			assertContentEquals(emptyList(), actions)
		}
	}

	@Test
	fun when_bottomBarItemsAreTapped_then_invokesOnNavigateToWithExpectedSections() = runTuIndiceUiTest {
		val sections = mutableListOf<MainSection>()

		withScreenKoin {
			setTuIndiceTestContent {
				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = browserStartDestination()
					),
					shellState = shellState(isBottomBarVisible = true),
					onRetryStartUp = {},
					onUpdateAppClick = {},
					navigator = rememberTestNavigator(startKey = browserStartDestination()),
					snackbarHostState = remember { SnackbarHostState() },
					onAction = {},
					onRecordViewModeChange = null,
					onRecordViewModeChangeAvailable = {},
					onNavigateTo = { section -> sections += section },
					onNavigateBack = {},
					onConfirmExitClick = {},
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			onNodeWithTag(MaincoreUiTags.TuIndiceBottomBarSummaryItem).performClick()
			onNodeWithTag(MaincoreUiTags.TuIndiceBottomBarRecordItem).performClick()
			onNodeWithTag(MaincoreUiTags.TuIndiceBottomBarEvaluationsItem).performClick()
			onNodeWithTag(MaincoreUiTags.TuIndiceBottomBarAboutItem).performClick()

			assertContentEquals(
				expected = listOf(
					MainSection.SUMMARY,
					MainSection.RECORD,
					MainSection.EVALUATIONS,
					MainSection.ABOUT
				),
				actual = sections
			)
		}
	}

	@Test
	fun when_browserRouteIsPopped_then_backButtonIsHiddenAgain() = runTuIndiceUiTest {
		lateinit var navigator: TuIndiceNavigator

		withScreenKoin {
			setTuIndiceTestContent {
				navigator = rememberTestNavigator(startKey = SummaryDestinationStart)

				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = SummaryDestinationStart
					),
					shellState = shellState(
						topBarTitle = "Privacidad",
						isTopBarVisible = true
					),
					onRetryStartUp = {},
					onUpdateAppClick = {},
					navigator = navigator,
					snackbarHostState = remember { SnackbarHostState() },
					onAction = {},
					onRecordViewModeChange = null,
					onRecordViewModeChangeAvailable = {},
					onNavigateTo = {},
					onNavigateBack = { navigator.pop() },
					onConfirmExitClick = {},
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			assertNodeHidden(MaincoreUiTags.TuIndiceTopBarBackButton)

			runOnIdle {
				navigator.push(browserStartDestination())
			}

			assertNodeVisible(MaincoreUiTags.TuIndiceTopBarBackButton)

			runOnIdle {
				navigator.pop()
			}

			assertNodeHidden(MaincoreUiTags.TuIndiceTopBarBackButton)
		}
	}

	@Test
	fun when_dialogIsDisplayed_then_backButtonIsHidden() = runTuIndiceUiTest {
		lateinit var navigator: TuIndiceNavigator

		withScreenKoin {
			setTuIndiceTestContent {
				navigator = rememberTestNavigator(startKey = SummaryDestinationStart)

				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = SummaryDestinationStart
					),
					shellState = shellState(
						topBarTitle = "Privacidad",
						isTopBarVisible = true
					),
					onRetryStartUp = {},
					onUpdateAppClick = {},
					navigator = navigator,
					snackbarHostState = remember { SnackbarHostState() },
					onAction = {},
					onRecordViewModeChange = null,
					onRecordViewModeChangeAvailable = {},
					onNavigateTo = {},
					onNavigateBack = { navigator.pop() },
					onConfirmExitClick = {},
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			runOnIdle {
				navigator.push(browserDetailDestination())
			}

			assertNodeVisible(MaincoreUiTags.TuIndiceTopBarBackButton)

			runOnIdle {
				navigator.push(MainDestination.GooglePlayServicesUnavailableDialog)
			}

			assertNodeHidden(MaincoreUiTags.TuIndiceTopBarBackButton)
		}
	}

	private inline fun withScreenKoin(block: () -> Unit) {
		stopKoin()
		startKoin {
			modules(testScreenModule())
		}

		try {
			block()
		} finally {
			stopKoin()
		}
	}

	private fun browserStartDestination(): Destination {
		return BrowserDestination.Browser(
			title = "Privacidad",
			url = "https://tuindice.app/privacy"
		)
	}

	private fun browserDetailDestination(): Destination {
		return BrowserDestination.Browser(
			title = "Términos",
			url = "https://tuindice.app/terms"
		)
	}

	private fun shellState(
		topBarTitle: String = "",
		topBarConfig: TopBarConfig? = null,
		isTopBarVisible: Boolean = false,
		isBottomBarVisible: Boolean = false,
		recordTopBarViewModeState: RecordTopBarViewModeState? = null
	): MainShellState = MainShellState(
		topBarTitle = if (topBarTitle.isBlank()) UiText.Empty else UiText.Raw(topBarTitle),
		topBarConfig = topBarConfig,
		isTopBarVisible = isTopBarVisible,
		isBottomBarVisible = isBottomBarVisible,
		recordTopBarViewModeState = recordTopBarViewModeState
	)

	private fun testScreenModule() = module {
		factory { createBrowserViewModel() }
		factory { createSummaryViewModel() }
		single<SyncRepository> { FakeSyncRepository() }
		single<SyncStatusRepository> { FakeSyncStatusRepository() }
		single<BrowserScreenRenderer> { TestBrowserRenderer }
	}

	private data object TestBrowserRenderer : BrowserScreenRenderer {
		@Composable
		override fun Render(
			url: String,
			modifier: Modifier,
			onPageStarted: () -> Unit,
			onPageFinished: () -> Unit,
			onPageError: () -> Unit,
			onExternalResourceClick: (url: String) -> Unit
		) {
			Text(text = "Browser: $url")
		}
	}

	private companion object {
		val SummaryDestinationStart: Destination =
			com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination.Summary
	}
}
