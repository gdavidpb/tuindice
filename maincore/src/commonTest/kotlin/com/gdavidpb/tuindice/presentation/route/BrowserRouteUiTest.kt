package com.gdavidpb.tuindice.presentation.route

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.presentation.contract.Browser
import com.gdavidpb.tuindice.testing.createBrowserViewModel
import com.gdavidpb.tuindice.testkit.ui.assertNodeHidden
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import com.gdavidpb.tuindice.ui.screen.BrowserScreenRenderer
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@OptIn(ExperimentalTestApi::class)
class BrowserRouteUiTest {
	@Test
	fun when_routeComposed_then_viewModelStateContainsProvidedTitleAndUrl() = runTuIndiceUiTest {
		val viewModel = createBrowserViewModel()
		var externalDialogUrl = ""

		stopKoin()

		startKoin {
			modules(
				module {
					single<BrowserScreenRenderer> { FakeBrowserRenderer }
				}
			)
		}

			try {
				setTuIndiceTestContent {
					BrowserRoute(
					title = "Politicas",
					url = "https://tuindice.app/privacy",
					onNavigateToExternalResourceDialog = { url ->
						externalDialogUrl = url
					},
					viewModel = viewModel
				)
			}

			waitUntil(timeoutMillis = 2_000) {
				val state = viewModel.state.value
				state is Browser.State.Content &&
					state.topBarTitle == "Politicas" &&
					state.url == "https://tuindice.app/privacy"
			}

			val contentState = assertIs<Browser.State.Content>(viewModel.state.value)
			assertEquals("Politicas", contentState.topBarTitle)
			assertEquals("https://tuindice.app/privacy", contentState.url)
			assertEquals("", externalDialogUrl)
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_externalResourceActionTriggered_then_navigatesToExternalDialog() = runTuIndiceUiTest {
		val viewModel = createBrowserViewModel()
		var externalDialogUrl = ""

		stopKoin()

		startKoin {
			modules(
				module {
					single<BrowserScreenRenderer> { FakeBrowserRenderer }
				}
			)
		}

		try {
			setTuIndiceTestContent {
				BrowserRoute(
					title = "Privacidad",
					url = "https://tuindice.app/privacy",
					onNavigateToExternalResourceDialog = { url ->
						externalDialogUrl = url
					},
					viewModel = viewModel
				)
			}

				waitUntil(timeoutMillis = 2_000) {
					onAllNodesWithText("Browser route: https://tuindice.app/privacy")
						.fetchSemanticsNodes().isNotEmpty()
				}

			runOnIdle {
				viewModel.openExternalResourceAction("https://externo.tuindice.app")
			}

			waitUntil(timeoutMillis = 2_000) {
				externalDialogUrl.isNotBlank()
			}

			assertEquals("https://externo.tuindice.app", externalDialogUrl)
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_rendererSignalsPageStart_then_routeShowsBrowserLoadingIndicator() = runTuIndiceUiTest {
		val viewModel = createBrowserViewModel()
		var pageStartedCalls = 0

		stopKoin()

		startKoin {
			modules(
				module {
					single<BrowserScreenRenderer> {
						object : BrowserScreenRenderer {
							@Composable
							override fun Render(
								url: String,
								modifier: Modifier,
								onPageStarted: () -> Unit,
								onPageFinished: () -> Unit,
								onExternalResourceClick: (url: String) -> Unit
							) {
								LaunchedEffect(url) {
									pageStartedCalls++
									onPageStarted()
								}

								Text(text = "Browser route loading: $url")
							}
						}
					}
				}
			)
		}

		try {
			setTuIndiceTestContent {
				BrowserRoute(
					title = "Ayuda",
					url = "https://tuindice.app/help",
					onNavigateToExternalResourceDialog = {},
					viewModel = viewModel
				)
			}

			waitUntil(timeoutMillis = 2_000) {
				pageStartedCalls > 0
			}

			assertNodeVisible(MaincoreUiTags.BrowserLoadingIndicator)
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_rendererSignalsPageFinished_then_routeHidesBrowserLoadingIndicator() = runTuIndiceUiTest {
		val viewModel = createBrowserViewModel()

		stopKoin()

		startKoin {
			modules(
				module {
					single<BrowserScreenRenderer> {
						object : BrowserScreenRenderer {
							@Composable
							override fun Render(
								url: String,
								modifier: Modifier,
								onPageStarted: () -> Unit,
								onPageFinished: () -> Unit,
								onExternalResourceClick: (url: String) -> Unit
							) {
								Button(onClick = onPageStarted) {
									Text(text = "Iniciar carga")
								}
								Button(onClick = onPageFinished) {
									Text(text = "Finalizar carga")
								}
							}
						}
					}
				}
			)
		}

		try {
			setTuIndiceTestContent {
				BrowserRoute(
					title = "Privacidad",
					url = "https://tuindice.app/privacy",
					onNavigateToExternalResourceDialog = {},
					viewModel = viewModel
				)
			}

			waitUntil(timeoutMillis = 2_000) {
				onAllNodesWithText("Iniciar carga", useUnmergedTree = true)
					.fetchSemanticsNodes().isNotEmpty()
			}

			onNodeWithText("Iniciar carga", useUnmergedTree = true).performClick()
			assertNodeVisible(MaincoreUiTags.BrowserLoadingIndicator)

			onNodeWithText("Finalizar carga", useUnmergedTree = true).performClick()

			waitUntil(timeoutMillis = 2_000) {
				onAllNodesWithTag(MaincoreUiTags.BrowserLoadingIndicator)
					.fetchSemanticsNodes().isEmpty()
			}

			assertNodeHidden(MaincoreUiTags.BrowserLoadingIndicator)
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_rendererRequestsExternalResource_then_routeNavigatesToExternalDialog() = runTuIndiceUiTest {
		val viewModel = createBrowserViewModel()
		var externalDialogUrl = ""

		stopKoin()

		startKoin {
			modules(
				module {
					single<BrowserScreenRenderer> {
						object : BrowserScreenRenderer {
							@Composable
							override fun Render(
								url: String,
								modifier: Modifier,
								onPageStarted: () -> Unit,
								onPageFinished: () -> Unit,
								onExternalResourceClick: (url: String) -> Unit
							) {
								LaunchedEffect(url) {
									onExternalResourceClick("https://externo.tuindice.app/trigger")
								}
								Text(text = "Browser route callback: $url")
							}
						}
					}
				}
			)
		}

		try {
			setTuIndiceTestContent {
				BrowserRoute(
					title = "Privacidad",
					url = "https://tuindice.app/privacy",
					onNavigateToExternalResourceDialog = { url ->
						externalDialogUrl = url
					},
					viewModel = viewModel
				)
			}

			waitUntil(timeoutMillis = 2_000) {
				externalDialogUrl.isNotBlank()
			}

			assertEquals("https://externo.tuindice.app/trigger", externalDialogUrl)
		} finally {
			stopKoin()
		}
	}

	@Test
	fun when_externalResourceIsRequestedFromRendererAfterTap_then_routeNavigatesToExternalDialog() = runTuIndiceUiTest {
		val viewModel = createBrowserViewModel()
		var externalDialogUrl = ""

		stopKoin()

		startKoin {
			modules(
				module {
					single<BrowserScreenRenderer> {
						object : BrowserScreenRenderer {
							@Composable
							override fun Render(
								url: String,
								modifier: Modifier,
								onPageStarted: () -> Unit,
								onPageFinished: () -> Unit,
								onExternalResourceClick: (url: String) -> Unit
							) {
								Button(
									onClick = { onExternalResourceClick("https://externo.tuindice.app/tap") }
								) {
									Text(text = "Abrir recurso externo")
								}
							}
						}
					}
				}
			)
		}

		try {
			setTuIndiceTestContent {
				BrowserRoute(
					title = "Privacidad",
					url = "https://tuindice.app/privacy",
					onNavigateToExternalResourceDialog = { url ->
						externalDialogUrl = url
					},
					viewModel = viewModel
					)
				}

				waitUntil(timeoutMillis = 2_000) {
					onAllNodesWithText("Abrir recurso externo").fetchSemanticsNodes().isNotEmpty()
				}

				onNodeWithText("Abrir recurso externo").performClick()

			waitUntil(timeoutMillis = 2_000) {
				externalDialogUrl.isNotBlank()
			}

			assertEquals("https://externo.tuindice.app/tap", externalDialogUrl)
		} finally {
			stopKoin()
		}
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
			Text(text = "Browser route: $url")
		}
	}
}
