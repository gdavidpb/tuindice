package com.gdavidpb.tuindice.about.testing

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.about.presentation.navigation.SettingsNavDependencies
import com.gdavidpb.tuindice.about.presentation.route.SettingsRoute
import com.gdavidpb.tuindice.about.presentation.viewmodel.SettingsViewModel
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage

// Where the rows of Settings lead, all answered with nothing: a test replaces the ones it looks at with `copy`. The
// photo is a plain text, as the host draws the real avatar.
fun noSettingsNavDependencies() = SettingsNavDependencies(
	onNavigateToBrowser = { _, _ -> },
	onNavigateToAbout = {},
	onNavigateToAssistantMemory = {},
	onNavigateToDeleteConfirmation = {},
	onSignOutRequest = {},
	profilePhoto = { identity -> Text(text = identity.pictureUrl) }
)

// The Settings route with what a test does not look at already answered, so each test names only what it checks. The
// rows that open the assistant's memory or its deletion dialog have no parameter here: those tests build the
// dependencies with `copy`.
@Composable
fun SettingsRouteUnderTest(
	viewModel: SettingsViewModel,
	onNavigateToBrowser: (title: String, url: String) -> Unit = { _, _ -> },
	onNavigateToAbout: () -> Unit = {},
	onSignOutRequest: () -> Unit = {},
	showSnackBar: (message: SnackBarMessage) -> Unit = {}
) {
	SettingsRoute(
		dependencies = noSettingsNavDependencies().copy(
			onNavigateToBrowser = onNavigateToBrowser,
			onNavigateToAbout = onNavigateToAbout,
			onSignOutRequest = onSignOutRequest
		),
		showSnackBar = showSnackBar,
		viewModel = viewModel
	)
}
