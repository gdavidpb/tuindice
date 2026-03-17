package com.gdavidpb.tuindice.ui.screen

import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
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
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.navigation.BrowserDestination
import com.gdavidpb.tuindice.presentation.navigation.MainDestination
import com.gdavidpb.tuindice.record.presentation.navigation.RecordDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.testing.createBrowserViewModel
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
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
				updateState = {},
				onRetryStartUp = {},
				navController = navController,
				snackbarHostState = remember { SnackbarHostState() },
				onAction = {},
				onNavigateTo = {},
				onNavigateBack = {},
				onConfirmExitClick = {},
				isCameraAvailable = false,
				onNavigateToExternalResource = {},
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
				updateState = {},
				onRetryStartUp = { retryCalls++ },
				navController = navController,
				snackbarHostState = remember { SnackbarHostState() },
				onAction = {},
				onNavigateTo = {},
				onNavigateBack = {},
				onConfirmExitClick = {},
				isCameraAvailable = false,
				onNavigateToExternalResource = {},
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
					startDestination = MainDestination.GooglePlayServicesUnavailableDialog,
					isBottomBarVisible = true
				),
				updateState = {},
				onRetryStartUp = {},
				navController = navController,
				snackbarHostState = remember { SnackbarHostState() },
				onAction = {},
				onNavigateTo = {},
				onNavigateBack = {},
				onConfirmExitClick = {},
				isCameraAvailable = false,
				onNavigateToExternalResource = {},
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
					startDestination = MainDestination.GooglePlayServicesUnavailableDialog,
					topBarTitle = "Resumen",
					topBarConfig = TopBarConfig.Summary,
					isTopBarVisible = true,
					isBottomBarVisible = false
				),
				updateState = {},
				onRetryStartUp = {},
				navController = navController,
				snackbarHostState = remember { SnackbarHostState() },
				onAction = {},
				onNavigateTo = {},
				onNavigateBack = {},
				onConfirmExitClick = {},
				isCameraAvailable = false,
				onNavigateToExternalResource = {},
				showSnackBar = {}
			)
		}

		assertNodeVisible(
			tag = BaseUiTags.topBarActionButton(TopBarAction.SignOutAction),
			useUnmergedTree = true
		)
	}

	@Test
	fun when_contentHidesBars_then_topBarAndBottomBarAreNotDisplayed() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val navController = rememberNavController()

			TuIndiceScreen(
				state = Main.State.Content(
					startDestination = MainDestination.GooglePlayServicesUnavailableDialog,
					topBarTitle = "Resumen",
					topBarConfig = TopBarConfig.Summary,
					isTopBarVisible = false,
					isBottomBarVisible = false
				),
				updateState = {},
				onRetryStartUp = {},
				navController = navController,
				snackbarHostState = remember { SnackbarHostState() },
				onAction = {},
				onNavigateTo = {},
				onNavigateBack = {},
				onConfirmExitClick = {},
				isCameraAvailable = false,
				onNavigateToExternalResource = {},
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
					startDestination = MainDestination.GooglePlayServicesUnavailableDialog,
					topBarTitle = "Inicio",
					topBarConfig = null,
					isTopBarVisible = false,
					isBottomBarVisible = true
				),
				updateState = {},
				onRetryStartUp = {},
				navController = navController,
				snackbarHostState = remember { SnackbarHostState() },
				onAction = {},
				onNavigateTo = {},
				onNavigateBack = {},
				onConfirmExitClick = {},
				isCameraAvailable = false,
				onNavigateToExternalResource = {},
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
						startDestination = browserStartDestination(),
						topBarTitle = "Resumen",
						topBarConfig = TopBarConfig.Summary,
						isTopBarVisible = true,
						isBottomBarVisible = false
					),
					updateState = {},
					onRetryStartUp = {},
					navController = navController,
					snackbarHostState = remember { SnackbarHostState() },
					onAction = { action -> actions += action },
					onNavigateTo = {},
					onNavigateBack = {},
					onConfirmExitClick = {},
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
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
	fun when_fetchEnrollmentProofActionTapped_then_invokesOnActionCallback() = runTuIndiceUiTest {
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
						startDestination = browserStartDestination(),
						topBarTitle = "Record",
						topBarConfig = TopBarConfig.Record,
						isTopBarVisible = true,
						isBottomBarVisible = false
					),
					updateState = {},
					onRetryStartUp = {},
					navController = navController,
					snackbarHostState = remember { SnackbarHostState() },
					onAction = { action -> actions += action },
					onNavigateTo = {},
					onNavigateBack = {},
					onConfirmExitClick = {},
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
					showSnackBar = {}
				)
			}

			onNodeWithTag(
				BaseUiTags.topBarActionButton(TopBarAction.FetchEnrollmentProofAction),
				useUnmergedTree = true
			).performClick()

			assertContentEquals(
				expected = listOf(TopBarAction.FetchEnrollmentProofAction),
				actual = actions
			)
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
						startDestination = browserStartDestination(),
						isTopBarVisible = false,
						isBottomBarVisible = true
					),
					updateState = {},
					onRetryStartUp = {},
					navController = navController,
					snackbarHostState = remember { SnackbarHostState() },
					onAction = {},
					onNavigateTo = { destination -> destinations += destination },
					onNavigateBack = {},
					onConfirmExitClick = {},
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
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
						startDestination = browserStartDestination(),
						topBarTitle = "Privacidad",
						isTopBarVisible = true,
						isBottomBarVisible = false
					),
					updateState = {},
					onRetryStartUp = {},
					navController = navController,
					snackbarHostState = remember { SnackbarHostState() },
					onAction = {},
					onNavigateTo = {},
					onNavigateBack = { navController.navigateUp() },
					onConfirmExitClick = {},
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
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

	private fun browserStartDestination(): Destination {
		return BrowserDestination.Browser(
			title = "Privacidad",
			url = "https://tuindice.app/privacy"
		)
	}

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
