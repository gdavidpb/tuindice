package com.gdavidpb.tuindice.ui.screen

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.presentation.navigation.BrowserDestination
import com.gdavidpb.tuindice.presentation.navigation.MainDestination
import com.gdavidpb.tuindice.presentation.navigation.TuIndiceNavigator
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.testing.createBrowserViewModel
import com.gdavidpb.tuindice.testing.createSummaryViewModel
import com.gdavidpb.tuindice.testing.rememberTestNavigator
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Retention lifetime semantics of the navigator-decided entry stores: popping
 * clears an entry's ViewModelStore, parking below the top retains it, and
 * dialog scenes keep the underlying entry composed.
 */
@OptIn(ExperimentalTestApi::class)
class TuIndiceNavRetentionUiTest {
	@Test
	fun when_entryPopped_then_itsViewModelStoreIsCleared() = runTuIndiceUiTest {
		var browserViewModelBuilds = 0
		lateinit var navigator: TuIndiceNavigator

		withRetentionKoin(onBrowserViewModelBuilt = { browserViewModelBuilds++ }) {
			setTuIndiceTestContent {
				navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

				RetentionNavDisplay(navigator = navigator)
			}

			runOnIdle { navigator.push(browserDestination()) }
			onNodeWithText("Browser: https://tuindice.app/privacy").assertExists()

			runOnIdle { navigator.pop() }
			assertNodeVisible(SummaryUiTags.ContentContainer)

			runOnIdle { navigator.push(browserDestination()) }
			onNodeWithText("Browser: https://tuindice.app/privacy").assertExists()

			runOnIdle {
				assertEquals(2, browserViewModelBuilds)
			}
		}
	}

	@Test
	fun when_entryParkedBelowTop_then_itsViewModelIsRetained() = runTuIndiceUiTest {
		var summaryViewModelBuilds = 0
		lateinit var navigator: TuIndiceNavigator

		withRetentionKoin(onSummaryViewModelBuilt = { summaryViewModelBuilds++ }) {
			setTuIndiceTestContent {
				navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

				RetentionNavDisplay(navigator = navigator)
			}

			assertNodeVisible(SummaryUiTags.ContentContainer)

			runOnIdle { navigator.push(browserDestination()) }
			onNodeWithText("Browser: https://tuindice.app/privacy").assertExists()

			runOnIdle { navigator.pop() }
			assertNodeVisible(SummaryUiTags.ContentContainer)

			runOnIdle {
				assertEquals(1, summaryViewModelBuilds)
			}
		}
	}

	@Test
	fun when_dialogDisplayed_then_underlyingEntryRemainsComposed() = runTuIndiceUiTest {
		lateinit var navigator: TuIndiceNavigator

		withRetentionKoin {
			setTuIndiceTestContent {
				navigator = rememberTestNavigator(startKey = SummaryDestination.Summary)

				RetentionNavDisplay(navigator = navigator)
			}

			runOnIdle { navigator.push(MainDestination.GooglePlayServicesUnavailableDialog) }

			assertNodeVisible(MaincoreUiTags.GooglePlayServicesMessage)
			assertNodeVisible(SummaryUiTags.ContentContainer)
		}
	}

	@Composable
	private fun RetentionNavDisplay(navigator: TuIndiceNavigator) {
		TuIndiceNavDisplay(
			navigator = navigator,
			onConfirmExitClick = {},
			isCameraAvailable = false,
			onNavigateToExternalResource = {},
			onRecordViewModeChangeAvailable = {},
			onRecordTermSelectionAvailable = {},
			showTopBarBanner = {},
			onViewStateChanged = {},
			showSnackBar = {}
		)
	}

	private fun browserDestination() = BrowserDestination.Browser(
		title = "Privacidad",
		url = "https://tuindice.app/privacy"
	)

	private inline fun withRetentionKoin(
		noinline onSummaryViewModelBuilt: () -> Unit = {},
		noinline onBrowserViewModelBuilt: () -> Unit = {},
		block: () -> Unit
	) {
		stopKoin()
		startKoin {
			modules(
				module {
					factory { createSummaryViewModel().also { onSummaryViewModelBuilt() } }
					factory { createBrowserViewModel().also { onBrowserViewModelBuilt() } }
					single<SyncRepository> { FakeSyncRepository() }
					single<SyncStatusRepository> { FakeSyncStatusRepository() }
					single<BrowserScreenRenderer> { RetentionTestBrowserRenderer }
				}
			)
		}

		try {
			block()
		} finally {
			stopKoin()
		}
	}

	private data object RetentionTestBrowserRenderer : BrowserScreenRenderer {
		@Composable
		override fun Render(
			url: String,
			modifier: Modifier,
			onPageStarted: () -> Unit,
			onPageFinished: () -> Unit,
			onPageError: () -> Unit,
			onExternalResourceClick: (url: String) -> Unit
		) {
			Text(text = "Browser: $url")
		}
	}
}
