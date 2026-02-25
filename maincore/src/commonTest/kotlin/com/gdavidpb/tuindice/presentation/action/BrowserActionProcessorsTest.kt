package com.gdavidpb.tuindice.presentation.action

import com.gdavidpb.tuindice.presentation.action.browser.NavigateToActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.OpenExternalResourceActionProcessor
import com.gdavidpb.tuindice.presentation.action.browser.SetLoadingActionProcessor
import com.gdavidpb.tuindice.presentation.contract.Browser
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class BrowserActionProcessorsTest {
	@Test
	fun navigateToActionProcessor_setsContentStateAsLoading() = runBlocking {
		val processor = NavigateToActionProcessor()

		val mutations = processor.process(
			action = Browser.Action.NavigateTo(
				title = "Privacy Policy",
				url = "https://tuindice.app/privacy"
			),
			sideEffect = {}
		).toList()
		val finalState = applyMutations(Browser.State.Idle, mutations)

		val content = assertIs<Browser.State.Content>(finalState)
		assertEquals("Privacy Policy", content.topBarTitle)
		assertEquals("https://tuindice.app/privacy", content.url)
		assertEquals(true, content.isLoading)
	}

	@Test
	fun setLoadingActionProcessor_updatesOnlyContentState() = runBlocking {
		val processor = SetLoadingActionProcessor()
		val contentState = Browser.State.Content(
			topBarTitle = "Title",
			url = "https://tuindice.app",
			isLoading = true
		)

		val mutations = processor.process(
			action = Browser.Action.SetLoading(isLoading = false),
			sideEffect = {}
		).toList()
		val finalState = applyMutations(contentState, mutations)

		val content = assertIs<Browser.State.Content>(finalState)
		assertEquals(false, content.isLoading)
	}

	@Test
	fun setLoadingActionProcessor_whenStateIsIdle_keepsIdle() = runBlocking {
		val processor = SetLoadingActionProcessor()

		val mutations = processor.process(
			action = Browser.Action.SetLoading(isLoading = true),
			sideEffect = {}
		).toList()
		val finalState = applyMutations(Browser.State.Idle, mutations)

		assertEquals(Browser.State.Idle, finalState)
	}

	@Test
	fun openExternalResourceActionProcessor_emitsDialogEffect() = runBlocking {
		val processor = OpenExternalResourceActionProcessor()
		val effects = mutableListOf<Browser.Effect>()
		val expectedUrl = "https://tuindice.app/external"

		processor.process(
			action = Browser.Action.OpenExternalResource(expectedUrl),
			sideEffect = effects::add
		).toList()

		val effect = assertIs<Browser.Effect.NavigateToExternalResourceDialog>(effects.single())
		assertEquals(expectedUrl, effect.url)
	}

	private fun applyMutations(
		initialState: Browser.State,
		mutations: List<(Browser.State) -> Browser.State>
	): Browser.State {
		return mutations.fold(initialState) { state, mutation -> mutation(state) }
	}
}
