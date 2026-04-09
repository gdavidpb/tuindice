package com.gdavidpb.tuindice.ui.screen

import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.gdavidpb.tuindice.about.presentation.navigation.AboutDestination
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.model.MainShellState
import com.gdavidpb.tuindice.presentation.navigation.BrowserDestination
import com.gdavidpb.tuindice.presentation.navigation.MainDestination
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.record.presentation.model.RecordTopBarViewModeState
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.testing.createBrowserViewModel
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.advanceAnimationsBy
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalTestApi::class)
class TuIndiceScreenUiTest {
	@Test
	fun when_stateIsStarting_then_displaysStartingIndicator() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val navController = rememberNavController()

			TuIndiceScreen(
				state = Main.State.Starting,
				shellState = shellState(),
				onRetryStartUp = {},
				navController = navController,
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
			val navController = rememberNavController()

			TuIndiceScreen(
				state = Main.State.Failed,
				shellState = shellState(),
				onRetryStartUp = { retryCalls++ },
				navController = navController,
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
	fun when_stateIsContentWithBottomBarVisible_then_displaysBottomBar() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val navController = rememberNavController()

			TuIndiceScreen(
				state = Main.State.Content(
					startDestination = MainDestination.GooglePlayServicesUnavailableDialog
				),
				shellState = shellState(isBottomBarVisible = true),
				onRetryStartUp = {},
				navController = navController,
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

	@Test
	fun when_topBarConfigIsSummary_then_displaysSignOutActionButton() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val navController = rememberNavController()

			TuIndiceScreen(
				state = Main.State.Content(
					startDestination = MainDestination.GooglePlayServicesUnavailableDialog
				),
				shellState = shellState(
					topBarTitle = "Resumen",
					topBarConfig = TopBarConfig.Summary,
					isTopBarVisible = true
				),
				onRetryStartUp = {},
				navController = navController,
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

	@Test
	fun when_recordTopBarViewModeStateIsPresent_then_displaysSwitchAndBanner() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val navController = rememberNavController()

			TuIndiceScreen(
				state = Main.State.Content(
					startDestination = MainDestination.GooglePlayServicesUnavailableDialog
				),
				shellState = shellState(
					topBarTitle = "Record",
					isTopBarVisible = true,
					recordTopBarViewModeState = RecordTopBarViewModeState(
						selectedMode = RecordViewMode.Working
					)
				),
				onRetryStartUp = {},
				navController = navController,
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
		assertNodeVisible(RecordUiTags.TopBarViewModeBanner)
	}

	@Test
	fun when_recordTopBarViewModeBannerTimeoutExpires_then_bannerIsHidden() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val navController = rememberNavController()

			TuIndiceScreen(
				state = Main.State.Content(
					startDestination = MainDestination.GooglePlayServicesUnavailableDialog
				),
				shellState = shellState(
					topBarTitle = "Record",
					isTopBarVisible = true,
					recordTopBarViewModeState = RecordTopBarViewModeState(
						selectedMode = RecordViewMode.Working
					)
				),
				onRetryStartUp = {},
				navController = navController,
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

		assertNodeVisible(RecordUiTags.TopBarViewModeBanner)

		advanceAnimationsBy(millis = 6_000)

		assertNodeHidden(RecordUiTags.TopBarViewModeBanner)
	}

	@Test
	fun when_recordTopBarViewModeChangesAfterBannerHides_then_bannerIsDisplayedAgain() = runTuIndiceUiTest {
		lateinit var shellStateState: MutableState<MainShellState>

		setTuIndiceTestContent {
			val navController = rememberNavController()
			shellStateState = remember {
				mutableStateOf(
					shellState(
						topBarTitle = "Record",
						isTopBarVisible = true,
						recordTopBarViewModeState = RecordTopBarViewModeState(
							selectedMode = RecordViewMode.Working
						)
					)
				)
			}

			TuIndiceScreen(
				state = Main.State.Content(
					startDestination = MainDestination.GooglePlayServicesUnavailableDialog
				),
				shellState = shellStateState.value,
				onRetryStartUp = {},
				navController = navController,
				snackbarHostState = remember { SnackbarHostState() },
				onAction = {},
				onRecordViewModeChange = { mode ->
					shellStateState.value = shellStateState.value.copy(
						recordTopBarViewModeState = RecordTopBarViewModeState(
							selectedMode = mode
						)
					)
				},
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

		assertNodeVisible(RecordUiTags.TopBarViewModeBanner)

		advanceAnimationsBy(millis = 6_000)

		assertNodeHidden(RecordUiTags.TopBarViewModeBanner)

		runOnIdle {
			shellStateState.value = shellStateState.value.copy(
				recordTopBarViewModeState = RecordTopBarViewModeState(
					selectedMode = RecordViewMode.Official
				)
			)
		}
		advanceAnimationsBy(millis = 300)

		assertNodeVisible(RecordUiTags.TopBarViewModeBanner)
	}

	@Test
	fun when_recordViewModeInfoSheetIsOpen_then_itStaysVisibleAfterBannerAutoHide() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val navController = rememberNavController()

			TuIndiceScreen(
				state = Main.State.Content(
					startDestination = MainDestination.GooglePlayServicesUnavailableDialog
				),
				shellState = shellState(
					topBarTitle = "Record",
					isTopBarVisible = true,
					recordTopBarViewModeState = RecordTopBarViewModeState(
						selectedMode = RecordViewMode.Working
					)
				),
				onRetryStartUp = {},
				navController = navController,
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

		onNodeWithTag(RecordUiTags.TopBarViewModeInfoButton).performClick()
		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithTag(BaseUiTags.ConfirmationDialogSheet).fetchSemanticsNodes().isNotEmpty()
		}

		assertNodeVisible(BaseUiTags.ConfirmationDialogSheet)

		advanceAnimationsBy(millis = 6_000)

		assertNodeHidden(RecordUiTags.TopBarViewModeBanner)
		assertNodeVisible(BaseUiTags.ConfirmationDialogSheet)
	}

	@Test
	fun when_contentHidesBars_then_topBarAndBottomBarAreNotDisplayed() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val navController = rememberNavController()

			TuIndiceScreen(
				state = Main.State.Content(
					startDestination = MainDestination.GooglePlayServicesUnavailableDialog
				),
				shellState = shellState(
					topBarTitle = "Resumen",
					topBarConfig = TopBarConfig.Summary
				),
				onRetryStartUp = {},
				navController = navController,
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

	@Test
	fun when_bottomBarIsVisible_then_exposesTaggedBottomBarItems() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val navController = rememberNavController()

			TuIndiceScreen(
				state = Main.State.Content(
					startDestination = MainDestination.GooglePlayServicesUnavailableDialog
				),
				shellState = shellState(
					topBarTitle = "Inicio",
					isBottomBarVisible = true
				),
				onRetryStartUp = {},
				navController = navController,
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

	@Test
	fun when_signOutActionTapped_then_invokesOnActionCallback() = runTuIndiceUiTest {
		val actions = mutableListOf<TopBarAction>()

		stopKoin()
		startKoin {
			modules(testBrowserModule())
		}

		try {
			setTuIndiceTestContent {
				val navController = rememberNavController()

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
					navController = navController,
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
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_recordViewModeSwitchTapped_then_invokesViewModeCallbackWithTopBarActionVisible() = runTuIndiceUiTest {
		val actions = mutableListOf<TopBarAction>()
		val selectedModes = mutableListOf<RecordViewMode>()

		stopKoin()
		startKoin {
			modules(testBrowserModule())
		}

		try {
			setTuIndiceTestContent {
				val navController = rememberNavController()

				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = browserStartDestination()
					),
					shellState = shellState(
						topBarTitle = "Record",
						isTopBarVisible = true,
						topBarConfig = TopBarConfig.Record,
						recordTopBarViewModeState = RecordTopBarViewModeState(
							selectedMode = RecordViewMode.Working
						)
					),
					onRetryStartUp = {},
					navController = navController,
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
				tag = BaseUiTags.topBarActionButton(TopBarAction.FetchEnrollmentProofAction),
				useUnmergedTree = true
			)
			onNodeWithTag(RecordUiTags.TopBarViewModeButton).performClick()

			assertContentEquals(
				expected = listOf(RecordViewMode.Official),
				actual = selectedModes
			)
			assertContentEquals(emptyList(), actions)
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_bottomBarItemsAreTapped_then_invokesOnNavigateToWithExpectedDestinations() = runTuIndiceUiTest {
		val destinations = mutableListOf<Destination>()

		stopKoin()
		startKoin {
			modules(testBrowserModule())
		}

		try {
			setTuIndiceTestContent {
				val navController = rememberNavController()

				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = browserStartDestination()
					),
					shellState = shellState(isBottomBarVisible = true),
					onRetryStartUp = {},
					navController = navController,
					snackbarHostState = remember { SnackbarHostState() },
					onAction = {},
					onRecordViewModeChange = null,
					onRecordViewModeChangeAvailable = {},
					onNavigateTo = { destination -> destinations += destination },
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
					SummaryDestination.NavGraph,
					RecordDestination.NavGraph,
					EvaluationsDestination.NavGraph,
					AboutDestination.NavGraph
				),
				actual = destinations
			)
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_browserRouteIsPopped_then_backButtonIsHiddenAgain() = runTuIndiceUiTest {
		lateinit var navController: NavHostController

		stopKoin()
		startKoin {
			modules(testBrowserModule())
		}

		try {
			setTuIndiceTestContent {
				navController = rememberNavController()

				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = browserStartDestination()
					),
					shellState = shellState(
						topBarTitle = "Privacidad",
						isTopBarVisible = true
					),
					onRetryStartUp = {},
					navController = navController,
					snackbarHostState = remember { SnackbarHostState() },
					onAction = {},
					onRecordViewModeChange = null,
					onRecordViewModeChangeAvailable = {},
					onNavigateTo = {},
					onNavigateBack = { navController.navigateUp() },
					onConfirmExitClick = {},
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			assertNodeHidden(MaincoreUiTags.TuIndiceTopBarBackButton)

			runOnIdle {
				navController.navigate(browserStartDestination())
			}

			assertNodeVisible(MaincoreUiTags.TuIndiceTopBarBackButton)

			runOnIdle {
				navController.navigateUp()
			}

			assertNodeHidden(MaincoreUiTags.TuIndiceTopBarBackButton)
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_dialogIsDisplayed_then_backButtonIsHidden() = runTuIndiceUiTest {
		lateinit var navController: NavHostController

		stopKoin()
		startKoin {
			modules(testBrowserModule())
		}

		try {
			setTuIndiceTestContent {
				navController = rememberNavController()

				TuIndiceScreen(
					state = Main.State.Content(
						startDestination = browserStartDestination()
					),
					shellState = shellState(
						topBarTitle = "Privacidad",
						isTopBarVisible = true
					),
					onRetryStartUp = {},
					navController = navController,
					snackbarHostState = remember { SnackbarHostState() },
					onAction = {},
					onRecordViewModeChange = null,
					onRecordViewModeChangeAvailable = {},
					onNavigateTo = {},
					onNavigateBack = { navController.navigateUp() },
					onConfirmExitClick = {},
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			runOnIdle {
				navController.navigate(browserDetailDestination())
			}

			assertNodeVisible(MaincoreUiTags.TuIndiceTopBarBackButton)

			runOnIdle {
				navController.navigate(MainDestination.GooglePlayServicesUnavailableDialog)
			}

			assertNodeHidden(MaincoreUiTags.TuIndiceTopBarBackButton)
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
		topBarTitle = topBarTitle,
		topBarConfig = topBarConfig,
		isTopBarVisible = isTopBarVisible,
		isBottomBarVisible = isBottomBarVisible,
		recordTopBarViewModeState = recordTopBarViewModeState
	)

	private fun testBrowserModule() = module {
		factory { createBrowserViewModel() }
		single<BrowserScreenRenderer> { TestBrowserRenderer }
	}

	private data object TestBrowserRenderer : BrowserScreenRenderer {
		@Composable
		override fun Render(
			url: String,
			modifier: Modifier,
			onPageStarted: () -> Unit,
			onPageFinished: () -> Unit,
			onExternalResourceClick: (url: String) -> Unit
		) {
			Text(text = "Browser: $url")
		}
	}
}
