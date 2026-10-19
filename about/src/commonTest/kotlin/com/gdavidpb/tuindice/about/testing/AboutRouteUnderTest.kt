package com.gdavidpb.tuindice.about.testing

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.about.presentation.route.AboutRoute
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage

// The About route with the snack bar a test does not look at already answered, so each test names only what it checks.
@Composable
fun AboutRouteUnderTest(
	viewModel: AboutViewModel,
	showSnackBar: (message: SnackBarMessage) -> Unit = {}
) {
	AboutRoute(
		showSnackBar = showSnackBar,
		viewModel = viewModel
	)
}
