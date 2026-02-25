package com.gdavidpb.tuindice.about.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.gdavidpb.tuindice.about.presentation.route.AboutRoute
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.ExternalActionsRepository
import org.koin.compose.koinInject

fun NavGraphBuilder.aboutNavigation(
	onNavigateToBrowser: (title: String, url: String) -> Unit
) {
	navigation<AboutDestination.NavGraph>(startDestination = AboutDestination.About) {
		composable<AboutDestination.About> {
			val browserGateway = koinInject<BrowserRepository>()
			val externalActions = koinInject<ExternalActionsRepository>()
			val viewModel = koinInject<AboutViewModel>()

			AboutRoute(
				onNavigateToBrowser = onNavigateToBrowser,
				browserGateway = browserGateway,
				externalActions = externalActions,
				viewModel = viewModel
			)
		}
	}
}
