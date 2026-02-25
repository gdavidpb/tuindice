package com.gdavidpb.tuindice.presentation.viewmodel

import com.gdavidpb.tuindice.presentation.action.browser.NavigateToActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.OpenExternalResourceActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.SetLoadingActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Browser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

class BrowserViewModelTest {
	@Test
	fun navigateAndLoadingActions_updateBrowserContentState() = runBlocking {
		val viewModel = BrowserViewModel(
			navigateToActionProcessor = NavigateToActionProcessor(),
			setLoadingActionProcessor = SetLoadingActionProcessor(),
			openExternalResourceActionProcessor = OpenExternalResourceActionProcessor()
		)
		val stateJob = launch { viewModel.state.collect() }

		try {
			waitUntil { viewModel.action.subscriptionCount.value > 0 }

			viewModel.navigateToAction(
				title = "Privacy Policy",
				url = "https://tuindice.app/privacy"
			)

			waitUntil {
				val state = viewModel.state.value
				state is Browser.State.Content && state.isLoading
			}
			val loading = assertIs<Browser.State.Content>(viewModel.state.value)
			assertEquals("Privacy Policy", loading.topBarTitle)
			assertEquals("https://tuindice.app/privacy", loading.url)
			assertEquals(true, loading.isLoading)

			viewModel.hideLoadingAction()

			waitUntil {
				val state = viewModel.state.value
				state is Browser.State.Content && !state.isLoading
			}
			val loaded = assertIs<Browser.State.Content>(viewModel.state.value)
			assertEquals(false, loaded.isLoading)
		} finally {
			stateJob.cancel()
		}
	}

	@Test
	fun openExternalResourceAction_emitsExternalNavigationEffect() = runBlocking {
		val viewModel = BrowserViewModel(
			navigateToActionProcessor = NavigateToActionProcessor(),
			setLoadingActionProcessor = SetLoadingActionProcessor(),
			openExternalResourceActionProcessor = OpenExternalResourceActionProcessor()
		)
		val effects = mutableListOf<Browser.Effect>()
		val effectJob = launch { viewModel.effect.collect { effects += it } }
		val stateJob = launch { viewModel.state.collect() }

		try {
			waitUntil { viewModel.action.subscriptionCount.value > 0 }

			viewModel.openExternalResourceAction("https://tuindice.app/external")

			waitUntil { effects.isNotEmpty() }
			val effect = assertIs<Browser.Effect.NavigateToExternalResourceDialog>(effects.single())
			assertEquals("https://tuindice.app/external", effect.url)
		} finally {
			effectJob.cancel()
			stateJob.cancel()
		}
	}

	private suspend fun waitUntil(
		timeoutMs: Long = 2_000L,
		condition: () -> Boolean
	) {
		val mark = TimeSource.Monotonic.markNow()

		while (!condition() && mark.elapsedNow() < timeoutMs.milliseconds) {
			delay(20)
		}

		assertTrue(condition(), "Condition not reached within timeout.")
	}
}
