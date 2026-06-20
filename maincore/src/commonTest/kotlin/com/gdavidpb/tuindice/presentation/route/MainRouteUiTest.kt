package com.gdavidpb.tuindice.presentation.route

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.base.domain.model.UpdateLaunchResult
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.testing.createMainViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeUpdateRepository
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalTestApi::class)
class MainRouteUiTest {
	@Test
	fun when_requestReviewActionTriggered_then_invokesReviewFlowCallback() = runTuIndiceUiTest {
		val viewModel = createMainViewModel()
		var reviewFlowCalls = 0
		var updateFlowCalls = 0
		var gmsDialogNavigations = 0
		var latestState: Main.State? = null

		setTuIndiceTestContent {
			MainRoute(
				onNavigateToGooglePlayServicesUnavailableDialog = {
					gmsDialogNavigations++
				},
				onRequestReviewFlow = {
					reviewFlowCalls++
				},
				onRequestUpdateFlow = {
					updateFlowCalls++
					UpdateLaunchResult.Launched
				},
				viewModel = viewModel
			) { state ->
				latestState = state
				Text(text = state::class.simpleName ?: "State")
			}
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithText("Content").fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithText("Content").assertIsDisplayed()

		runOnIdle {
			viewModel.requestReviewAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			reviewFlowCalls > 0
		}

		assertEquals(1, reviewFlowCalls)
		assertEquals(0, updateFlowCalls)
		assertEquals(0, gmsDialogNavigations)
		assertIs<Main.State.Content>(latestState)
	}

	@Test
	fun when_requestUpdateActionTriggered_then_invokesUpdateFlowCallback() = runTuIndiceUiTest {
		val viewModel = createMainViewModel(
			updateRepository = FakeUpdateRepository(
				updateAction = UpdateAction.Immediate
			)
		)
		var reviewFlowCalls = 0
		val updateFlowActions = mutableListOf<UpdateAction>()
		var latestState: Main.State? = null

		setTuIndiceTestContent {
			MainRoute(
				onNavigateToGooglePlayServicesUnavailableDialog = {},
				onRequestReviewFlow = {
					reviewFlowCalls++
				},
				onRequestUpdateFlow = { action ->
					updateFlowActions += action
					UpdateLaunchResult.Launched
				},
				viewModel = viewModel
			) { state ->
				latestState = state
				Text(text = state::class.simpleName ?: "State")
			}
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithText("Content").fetchSemanticsNodes().isNotEmpty()
		}

		onNodeWithText("Content").assertIsDisplayed()

		runOnIdle {
			viewModel.checkUpdateAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			updateFlowActions.isNotEmpty()
		}

		assertEquals(listOf(UpdateAction.Immediate), updateFlowActions)
		assertEquals(0, reviewFlowCalls)
		assertIs<Main.State.Content>(latestState)
	}

	@Test
	fun when_updateFlowReturnsStoreFallback_then_emitsFallbackCallback() = runTuIndiceUiTest {
		val viewModel = createMainViewModel(
			updateRepository = FakeUpdateRepository(
				updateAction = UpdateAction.Immediate
			)
		)
		val fallbackResult = UpdateLaunchResult.OpenStoreFallback(
			primaryUrl = "market://details?id=com.gdavidpb.tuindice",
			fallbackUrl = "https://play.google.com/store/apps/details?id=com.gdavidpb.tuindice"
		)
		val fallbackResults = mutableListOf<UpdateLaunchResult.OpenStoreFallback>()

		setTuIndiceTestContent {
			MainRoute(
				onNavigateToGooglePlayServicesUnavailableDialog = {},
				onRequestReviewFlow = {},
				onRequestUpdateFlow = {
					fallbackResult
				},
				onOpenUpdateStoreFallback = { result ->
					fallbackResults += result
				},
				viewModel = viewModel
			) { state ->
				Text(text = state::class.simpleName ?: "State")
			}
		}

		waitUntil(timeoutMillis = 2_000) {
			onAllNodesWithText("Content").fetchSemanticsNodes().isNotEmpty()
		}

		runOnIdle {
			viewModel.checkUpdateAction()
		}

		waitUntil(timeoutMillis = 2_000) {
			fallbackResults.isNotEmpty()
		}

		assertEquals(listOf(fallbackResult), fallbackResults)
	}

	@Test
	fun when_startUpFailsWithNoGooglePlayServices_then_navigatesToGmsDialog() = runTuIndiceUiTest {
		val viewModel = createMainViewModel(
			sessionRepository = GooglePlayServicesFailingSessionRepository()
		)
		var gmsDialogNavigations = 0
		var reviewFlowCalls = 0
		var updateFlowCalls = 0
		var latestState: Main.State? = null

		setTuIndiceTestContent {
			MainRoute(
				onNavigateToGooglePlayServicesUnavailableDialog = {
					gmsDialogNavigations++
				},
				onRequestReviewFlow = {
					reviewFlowCalls++
				},
				onRequestUpdateFlow = {
					updateFlowCalls++
					UpdateLaunchResult.Launched
				},
				viewModel = viewModel
			) { state ->
				latestState = state
				Text(text = state::class.simpleName ?: "State")
			}
		}

		waitUntil(timeoutMillis = 2_000) {
			gmsDialogNavigations > 0 && latestState is Main.State.Failed
		}

		assertEquals(1, gmsDialogNavigations)
		assertEquals(0, reviewFlowCalls)
		assertEquals(0, updateFlowCalls)
		assertIs<Main.State.Failed>(latestState)
	}

	@Test
	fun when_startUpSucceeds_then_routeExposesResolvedStartDestination() = runTuIndiceUiTest {
		val viewModel = createMainViewModel()
		var latestState: Main.State? = null

		setTuIndiceTestContent {
			MainRoute(
				onNavigateToGooglePlayServicesUnavailableDialog = {},
				onRequestReviewFlow = {},
				onRequestUpdateFlow = { UpdateLaunchResult.Launched },
				viewModel = viewModel
			) { state ->
				latestState = state
				Text(text = state::class.simpleName ?: "State")
			}
		}

		waitUntil(timeoutMillis = 2_000) {
			(latestState as? Main.State.Content)?.startDestination == SummaryDestination.NavGraph
		}

		assertEquals(SummaryDestination.NavGraph, (latestState as Main.State.Content).startDestination)
	}

	@Test
	fun when_appAvailabilityNoticeIsEnabled_then_routeExposesAppUnavailableState() = runTuIndiceUiTest {
		val notice = AppAvailabilityNotice(
			enabled = true,
			title = "Servicio pausado",
			message = "Estamos en mantenimiento."
		)
		val viewModel = createMainViewModel(
			configRepository = FakeConfigRepository(appAvailabilityNotice = notice)
		)
		var latestState: Main.State? = null

		setTuIndiceTestContent {
			MainRoute(
				onNavigateToGooglePlayServicesUnavailableDialog = {},
				onRequestReviewFlow = {},
				onRequestUpdateFlow = { UpdateLaunchResult.Launched },
				viewModel = viewModel
			) { state ->
				latestState = state
				Text(text = state::class.simpleName ?: "State")
			}
		}

		waitUntil(timeoutMillis = 2_000) {
			latestState is Main.State.AppUnavailable
		}

		assertEquals(notice, (latestState as Main.State.AppUnavailable).notice)
	}

	private class GooglePlayServicesFailingSessionRepository : SessionRepository {
		override suspend fun hasActiveSession(): Boolean {
			throw GooglePlayServicesNotAvailableException()
		}

		override suspend fun getActiveSessionSnapshot(): SessionSnapshot? {
			throw GooglePlayServicesNotAvailableException()
		}

		override suspend fun setSessionSnapshot(snapshot: SessionSnapshot) = Unit

		override suspend fun replaceSessionSnapshotIfCurrent(
			expectedSnapshot: SessionSnapshot,
			newSnapshot: SessionSnapshot
		): Boolean = true

		override suspend fun setUsbId(usbId: String) = Unit

		override suspend fun setSessionId(sessionId: String) = Unit

		override suspend fun setAccessToken(accessToken: String) = Unit

		override suspend fun setRefreshToken(refreshToken: String) = Unit

		override suspend fun getUsbId(): String = ""

		override suspend fun getSessionId(): String = ""

		override suspend fun getAccessToken(): String = ""

		override suspend fun getRefreshToken(): String = ""

		override suspend fun clear() = Unit
	}

	private class GooglePlayServicesNotAvailableException : RuntimeException()
}
