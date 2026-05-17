package com.gdavidpb.tuindice.ui.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.presentation.contract.Browser
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class BrowserScreenUiTest {
	@Test
	fun when_stateIsContent_then_rendersBrowserContent() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			BrowserScreen(
				state = Browser.State.Content(
					topBarTitle = UiText.Raw("Navegador"),
					url = "https://tuindice.app/privacy",
					isLoading = false
				),
				onPageStarted = {},
				onPageFinished = {},
				onExternalResourceClick = {},
				renderer = FakeBrowserRenderer
			)
		}

		assertNodeVisible(MaincoreUiTags.BrowserContainer)
		onNodeWithText("Render URL: https://tuindice.app/privacy").assertIsDisplayed()
	}

	@Test
	fun when_browserIsLoading_then_displaysLinearProgress() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			BrowserScreen(
				state = Browser.State.Content(
					topBarTitle = UiText.Raw("Navegador"),
					url = "https://tuindice.app/terms",
					isLoading = true
				),
				onPageStarted = {},
				onPageFinished = {},
				onExternalResourceClick = {},
				renderer = FakeBrowserRenderer
			)
		}

		assertNodeVisible(MaincoreUiTags.BrowserLoadingIndicator)
	}

	@Test
	fun when_stateIsIdle_then_hidesBrowserContainer() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			BrowserScreen(
				state = Browser.State.Idle,
				onPageStarted = {},
				onPageFinished = {},
				onExternalResourceClick = {},
				renderer = FakeBrowserRenderer
			)
		}

		assertNodeHidden(MaincoreUiTags.BrowserContainer)
		assertNodeHidden(MaincoreUiTags.BrowserLoadingIndicator)
	}

	@Test
	fun when_browserIsNotLoading_then_hidesLinearProgress() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			BrowserScreen(
				state = Browser.State.Content(
					topBarTitle = UiText.Raw("Navegador"),
					url = "https://tuindice.app/help",
					isLoading = false
				),
				onPageStarted = {},
				onPageFinished = {},
				onExternalResourceClick = {},
				renderer = FakeBrowserRenderer
			)
		}

		assertNodeVisible(MaincoreUiTags.BrowserContainer)
		assertNodeHidden(MaincoreUiTags.BrowserLoadingIndicator)
	}

	@Test
	fun when_rendererFiresCallbacks_then_browserScreenForwardsThem() = runTuIndiceUiTest {
		var pageStartedCalls = 0
		var pageFinishedCalls = 0
		var externalResourceUrl = ""

		val renderer = object : BrowserScreenRenderer {
			@Composable
			override fun Render(
				url: String,
				modifier: Modifier,
				onPageStarted: () -> Unit,
				onPageFinished: () -> Unit,
				onExternalResourceClick: (url: String) -> Unit
			) {
				LaunchedEffect(url) {
					onPageStarted()
					onPageFinished()
					onExternalResourceClick("https://externo.tuindice.app")
				}

				Text(text = "Render URL: $url")
			}
		}

		setTuIndiceTestContent {
			BrowserScreen(
				state = Browser.State.Content(
					topBarTitle = UiText.Raw("Navegador"),
					url = "https://tuindice.app/privacy",
					isLoading = false
				),
				onPageStarted = { pageStartedCalls++ },
				onPageFinished = { pageFinishedCalls++ },
				onExternalResourceClick = { url -> externalResourceUrl = url },
				renderer = renderer
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			pageStartedCalls > 0 &&
				pageFinishedCalls > 0 &&
				externalResourceUrl.isNotBlank()
		}

		assertEquals(1, pageStartedCalls)
		assertEquals(1, pageFinishedCalls)
		assertEquals("https://externo.tuindice.app", externalResourceUrl)
	}

	private data object FakeBrowserRenderer : BrowserScreenRenderer {
		@Composable
		override fun Render(
			url: String,
			modifier: Modifier,
			onPageStarted: () -> Unit,
			onPageFinished: () -> Unit,
			onExternalResourceClick: (url: String) -> Unit
		) {
			Text(text = "Render URL: $url")
		}
	}
}
