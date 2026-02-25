package com.gdavidpb.tuindice.about.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.about.presentation.route.AboutRoute
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.ExternalActionsRepository
import com.gdavidpb.tuindice.base.presentation.ViewState
import org.koin.compose.koinInject

fun NavGraphBuilder.aboutNavigation(
	onNavigateToBrowser: (title: String, url: String) -> Unit,
	onViewStateChanged: (ViewState) -> Unit
) {
	navigation<AboutDestination.NavGraph>(startDestination = AboutDestination.About) {
		composable<AboutDestination.About> {
			val browserGateway = koinInject<BrowserRepository>()
			val externalActions = koinInject<ExternalActionsRepository>()
			val viewModel = koinInject<AboutViewModel>()
			val viewState by viewModel.state.collectAsStateWithLifecycle()

			LaunchedEffect(viewState) {
				onViewStateChanged(viewState)
			}

			AboutRoute(
				onNavigateToBrowser = onNavigateToBrowser,
				browserGateway = browserGateway,
				externalActions = externalActions,
				viewModel = viewModel
			)
		}
	}
}
