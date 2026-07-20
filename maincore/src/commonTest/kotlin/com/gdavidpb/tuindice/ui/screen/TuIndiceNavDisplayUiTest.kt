package com.gdavidpb.tuindice.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.presentation.navigation.MainDestination
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
 * Dialog-as-start canary: a dialog start key must render as an overlay above
 * the synthesized start tab (Summary), never as a blank display.
 */
@OptIn(ExperimentalTestApi::class)
class TuIndiceNavDisplayUiTest {
	@Test
	fun when_navDisplayRendered_then_displaysMainNavigationStartDestination() = runTuIndiceUiTest {
		withDisplayKoin {
			setTuIndiceTestContent {
				TuIndiceNavDisplay(
					navigator = rememberTestNavigator(
						startKey = MainDestination.GooglePlayServicesUnavailableDialog
					),
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

			assertNodeVisible(MaincoreUiTags.GooglePlayServicesMessage)
		}
	}

	@Test
	fun when_navDisplayRendered_then_doesNotInvokeConfirmExitInitially() = runTuIndiceUiTest {
		var confirmExitCalls = 0

		withDisplayKoin {
			setTuIndiceTestContent {
				TuIndiceNavDisplay(
					navigator = rememberTestNavigator(
						startKey = MainDestination.GooglePlayServicesUnavailableDialog
					),
					onConfirmExitClick = { confirmExitCalls++ },
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
					onRecordViewModeChangeAvailable = {},
					onRecordTermSelectionAvailable = {},
					showTopBarBanner = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			assertNodeVisible(MaincoreUiTags.GooglePlayServicesMessage)
			assertEquals(0, confirmExitCalls)
		}
	}

	@Test
	fun when_googlePlayServicesExitTapped_then_invokesConfirmExitCallback() = runTuIndiceUiTest {
		var confirmExitCalls = 0

		withDisplayKoin {
			setTuIndiceTestContent {
				TuIndiceNavDisplay(
					navigator = rememberTestNavigator(
						startKey = MainDestination.GooglePlayServicesUnavailableDialog
					),
					onConfirmExitClick = { confirmExitCalls++ },
					isCameraAvailable = false,
					onNavigateToExternalResource = {},
					onRecordViewModeChangeAvailable = {},
					onRecordTermSelectionAvailable = {},
					showTopBarBanner = {},
					onViewStateChanged = {},
					showSnackBar = {}
				)
			}

			assertNodeVisible(BaseUiTags.ConfirmationDialogPositiveButton)
			onNodeWithTag(BaseUiTags.ConfirmationDialogPositiveButton).performClick()

			waitUntil(timeoutMillis = 2_000) {
				confirmExitCalls > 0
			}

			assertEquals(1, confirmExitCalls)
		}
	}

	private inline fun withDisplayKoin(block: () -> Unit) {
		stopKoin()
		startKoin {
			modules(
				module {
					factory { createSummaryViewModel() }
					single<SyncRepository> { FakeSyncRepository() }
					single<SyncStatusRepository> { FakeSyncStatusRepository() }
				}
			)
		}

		try {
			block()
		} finally {
			stopKoin()
		}
	}
}
