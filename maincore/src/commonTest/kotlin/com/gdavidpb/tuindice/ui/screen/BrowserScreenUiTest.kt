package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.presentation.contract.Browser
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import com.gdavidpb.tuindice.ui.MaincoreUiTags
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
				onPageError = {},
				onRetryClick = {},
				onExternalResourceClick = {},
				renderer = FakeBrowserRenderer
			)
		}

		assertNodeVisible(MaincoreUiTags.BrowserContainer)
		assertNodeHidden(MaincoreUiTags.BrowserE2eExternalResourceTrigger)
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
				onPageError = {},
				onRetryClick = {},
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
				onPageError = {},
				onRetryClick = {},
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
				onPageError = {},
				onRetryClick = {},
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
		var pageErrorCalls = 0
		var externalResourceUrl = ""

		val renderer = object : BrowserScreenRenderer {
			@Composable
			override fun Render(
				url: String,
				modifier: Modifier,
				onPageStarted: () -> Unit,
				onPageFinished: () -> Unit,
				onPageError: () -> Unit,
				onExternalResourceClick: (url: String) -> Unit
			) {
				LaunchedEffect(url) {
					onPageStarted()
					onPageFinished()
					onPageError()
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
				onPageError = { pageErrorCalls++ },
				onRetryClick = {},
				onExternalResourceClick = { url -> externalResourceUrl = url },
				renderer = renderer
			)
		}

		waitUntil(timeoutMillis = 2_000) {
			pageStartedCalls > 0 &&
				pageFinishedCalls > 0 &&
				pageErrorCalls > 0 &&
				externalResourceUrl.isNotBlank()
		}

		assertEquals(1, pageStartedCalls)
		assertEquals(1, pageFinishedCalls)
		assertEquals(1, pageErrorCalls)
		assertEquals("https://externo.tuindice.app", externalResourceUrl)
	}

	@Test
	fun when_browserFailedToLoad_then_displaysErrorViewWithRetry() = runTuIndiceUiTest {
		var retryCalls = 0

		setTuIndiceTestContent {
			BrowserScreen(
				state = Browser.State.Content(
					topBarTitle = UiText.Raw("Navegador"),
					url = "https://tuindice.app/privacy",
					isLoading = false,
					hasError = true
				),
				onPageStarted = {},
				onPageFinished = {},
				onPageError = {},
				onRetryClick = { retryCalls++ },
				onExternalResourceClick = {},
				renderer = FakeBrowserRenderer
			)
		}

		assertNodeHidden(MaincoreUiTags.BrowserContainer)
		assertNodeVisible(BaseUiTags.ErrorViewRetryButton)
		onNodeWithTag(BaseUiTags.ErrorViewRetryButton).performClick()
		assertEquals(1, retryCalls)
	}

	@Test
	fun when_browserLoadsLocalE2ePage_then_exposesExternalResourceTrigger() = runTuIndiceUiTest {
		var externalResourceUrl = ""

		setTuIndiceTestContent {
			BrowserScreen(
				state = Browser.State.Content(
					topBarTitle = UiText.Raw("Navegador"),
					url = "http://127.0.0.1:8080/e2e/privacy.html",
					isLoading = true
				),
				onPageStarted = {},
				onPageFinished = {},
				onPageError = {},
				onRetryClick = {},
				onExternalResourceClick = { url -> externalResourceUrl = url },
				renderer = FakeBrowserRenderer
			)
		}

		assertNodeVisible(MaincoreUiTags.BrowserE2eExternalResourceTrigger)
		onNodeWithTag(MaincoreUiTags.BrowserE2eExternalResourceTrigger).performClick()

		assertEquals("https://external.tuindice.test/maincore-e2e", externalResourceUrl)
	}

	private data object FakeBrowserRenderer : BrowserScreenRenderer {
		@Composable
		override fun Render(
			url: String,
			modifier: Modifier,
			onPageStarted: () -> Unit,
			onPageFinished: () -> Unit,
			onPageError: () -> Unit,
			onExternalResourceClick: (url: String) -> Unit
		) {
			Column(modifier = modifier) {
				Text(text = "Render URL: $url")
				if (url.endsWith("/e2e/privacy.html")) {
					TextButton(
						modifier = Modifier.testTag(MaincoreUiTags.BrowserE2eExternalResourceTrigger),
						onClick = {
							onExternalResourceClick("https://external.tuindice.test/maincore-e2e")
						}
					) {
						Text(text = "Abrir enlace externo")
					}
				}
			}
		}
	}
}
