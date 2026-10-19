package com.gdavidpb.tuindice.about.testing

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.about.presentation.route.SettingsRoute
import com.gdavidpb.tuindice.about.presentation.viewmodel.SettingsViewModel
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage

// The Settings route with the callbacks a test does not look at already answered, so each test names only what it checks.
// The photo is a plain text, as the host draws the real avatar.
@Composable
fun SettingsRouteUnderTest(
	viewModel: SettingsViewModel,
	onNavigateToBrowser: (title: String, url: String) -> Unit = { _, _ -> },
	onNavigateToAbout: () -> Unit = {},
	onNavigateToAssistantMemory: () -> Unit = {},
	onNavigateToDeleteConfirmation: () -> Unit = {},
	onSignOutRequest: () -> Unit = {},
	showSnackBar: (message: SnackBarMessage) -> Unit = {}
) {
	SettingsRoute(
		onNavigateToBrowser = onNavigateToBrowser,
		onNavigateToAbout = onNavigateToAbout,
		onNavigateToAssistantMemory = onNavigateToAssistantMemory,
		onNavigateToDeleteConfirmation = onNavigateToDeleteConfirmation,
		onSignOutRequest = onSignOutRequest,
		showSnackBar = showSnackBar,
		viewModel = viewModel,
		profilePhoto = { identity -> Text(text = identity.pictureUrl) }
	)
}
