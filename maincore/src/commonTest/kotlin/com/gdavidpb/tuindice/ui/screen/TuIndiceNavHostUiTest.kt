package com.gdavidpb.tuindice.ui.screen

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.presentation.navigation.MainDestination
import com.gdavidpb.tuindice.testkit.ui.assertNodeVisible
import com.gdavidpb.tuindice.testkit.ui.runTuIndiceUiTest
import com.gdavidpb.tuindice.testkit.ui.setTuIndiceTestContent
import com.gdavidpb.tuindice.ui.MaincoreUiTags
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class TuIndiceNavHostUiTest {
	@Test
	fun when_navHostRendered_then_displaysMainNavigationStartDestination() = runTuIndiceUiTest {
		setTuIndiceTestContent {
			val navController = rememberNavController()

			TuIndiceNavHost(
				navController = navController,
				startDestination = MainDestination.GooglePlayServicesUnavailableDialog,
				onConfirmExitClick = {},
				isCameraAvailable = false,
				onNavigateToExternalResource = {},
				onViewStateChanged = {},
				showSnackBar = {}
			)
		}

		assertNodeVisible(MaincoreUiTags.GooglePlayServicesMessage)
	}

	@Test
	fun when_navHostRendered_then_doesNotInvokeConfirmExitInitially() = runTuIndiceUiTest {
		var confirmExitCalls = 0

		setTuIndiceTestContent {
			val navController = rememberNavController()

			TuIndiceNavHost(
				navController = navController,
				startDestination = MainDestination.GooglePlayServicesUnavailableDialog,
				onConfirmExitClick = { confirmExitCalls++ },
				isCameraAvailable = false,
				onNavigateToExternalResource = {},
				onViewStateChanged = {},
				showSnackBar = {}
			)
		}

		assertNodeVisible(MaincoreUiTags.GooglePlayServicesMessage)
		assertEquals(0, confirmExitCalls)
	}

	@Test
	fun when_googlePlayServicesExitTapped_then_invokesConfirmExitCallback() = runTuIndiceUiTest {
		var confirmExitCalls = 0

		setTuIndiceTestContent {
			val navController = rememberNavController()

			TuIndiceNavHost(
				navController = navController,
				startDestination = MainDestination.GooglePlayServicesUnavailableDialog,
				onConfirmExitClick = { confirmExitCalls++ },
				isCameraAvailable = false,
				onNavigateToExternalResource = {},
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
