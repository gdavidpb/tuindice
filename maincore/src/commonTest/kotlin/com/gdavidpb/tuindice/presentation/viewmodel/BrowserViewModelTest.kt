package com.gdavidpb.tuindice.presentation.viewmodel

import com.gdavidpb.tuindice.presentation.action.browser.NavigateToActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.OpenExternalResourceActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.SetLoadingActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Browser
import kotlinx.coroutines.CoroutineStart
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
		val stateJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.state.collect() }

		try {
			viewModel.navigateToAction(
				title = "Privacy Policy",
				url = "https://tuindice.app/privacy"
			)

			waitUntil("state reached loading content") {
				val state = viewModel.state.value
				state is Browser.State.Content && state.isLoading
			}
			val loading = assertIs<Browser.State.Content>(viewModel.state.value)
			assertEquals("Privacy Policy", loading.topBarTitle)
			assertEquals("https://tuindice.app/privacy", loading.url)
			assertEquals(true, loading.isLoading)

			viewModel.hideLoadingAction()

			waitUntil("state reached non-loading content") {
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
	fun openExternalResourceAction_doesNotMutateState() = runBlocking {
		val viewModel = BrowserViewModel(
			navigateToActionProcessor = NavigateToActionProcessor(),
			setLoadingActionProcessor = SetLoadingActionProcessor(),
			openExternalResourceActionProcessor = OpenExternalResourceActionProcessor()
		)
		val stateJob = launch(start = CoroutineStart.UNDISPATCHED) { viewModel.state.collect() }

		try {
			viewModel.openExternalResourceAction("https://tuindice.app/external")

			waitUntil("state remains idle after external resource action") {
				viewModel.state.value == Browser.State.Idle
			}
		} finally {
			stateJob.cancel()
		}
	}

	private suspend fun waitUntil(
		label: String,
		timeoutMs: Long = 5_000L,
		condition: () -> Boolean
	) {
		val mark = TimeSource.Monotonic.markNow()

		while (!condition() && mark.elapsedNow() < timeoutMs.milliseconds) {
			delay(20)
		}

		assertTrue(condition(), "Condition not reached within timeout: $label")
	}
}
