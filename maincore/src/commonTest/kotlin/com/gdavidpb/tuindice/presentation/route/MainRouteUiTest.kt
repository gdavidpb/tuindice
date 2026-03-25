package com.gdavidpb.tuindice.presentation.route

import androidx.compose.material3.Text
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.model.UpdateAction
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.navigation.MainDestination
import com.gdavidpb.tuindice.testing.createMainViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakeUpdateRepository
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

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
				},
				viewModel = viewModel
			) { state, _ ->
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
				},
				viewModel = viewModel
			) { state, _ ->
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
				},
				viewModel = viewModel
			) { state, _ ->
				latestState = state
				Text(text = state::class.simpleName ?: "State")
			}
		}

		waitUntil(timeoutMillis = 2_000) {
			gmsDialogNavigations > 0
		}

		assertEquals(1, gmsDialogNavigations)
		assertEquals(0, reviewFlowCalls)
		assertEquals(0, updateFlowCalls)
		assertIs<Main.State.Failed>(latestState)
	}

	@Test
	fun when_contentUpdatesStateViaCallback_then_routeRendersUpdatedState() = runTuIndiceUiTest {
		val viewModel = createMainViewModel()
		var latestState: Main.State? = null
		var updateStateCallback: ((Main.State) -> Unit)? = null

		setTuIndiceTestContent {
			MainRoute(
				onNavigateToGooglePlayServicesUnavailableDialog = {},
				onRequestReviewFlow = {},
				onRequestUpdateFlow = {},
				viewModel = viewModel
			) { state, updateState ->
				latestState = state
				updateStateCallback = updateState

				val text = when (state) {
					is Main.State.Content ->
						state.topBarTitle.ifBlank { "Sin titulo" }

					else ->
						state::class.simpleName ?: "State"
				}

				Text(text = text)
			}
		}

		onNodeWithText("Sin titulo").assertIsDisplayed()

		runOnIdle {
			val callback = assertNotNull(updateStateCallback)
			callback(
				Main.State.Content(
					startDestination = MainDestination.GooglePlayServicesUnavailableDialog,
					topBarTitle = "Titulo actualizado"
				)
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			(latestState as? Main.State.Content)?.topBarTitle == "Titulo actualizado"
		}

		onNodeWithText("Titulo actualizado").assertIsDisplayed()
	}

	private class GooglePlayServicesFailingSessionRepository : SessionRepository {
		override suspend fun hasActiveSession(): Boolean {
			throw GooglePlayServicesNotAvailableException()
		}

		override suspend fun setUsbId(usbId: String) = Unit

		override suspend fun setAccessToken(accessToken: String) = Unit

		override suspend fun setRefreshToken(refreshToken: String) = Unit

		override suspend fun getUsbId(): String = ""

		override suspend fun getAccessToken(): String = ""

		override suspend fun getRefreshToken(): String = ""

		override suspend fun clear() = Unit
	}

	private class GooglePlayServicesNotAvailableException : RuntimeException()
}
