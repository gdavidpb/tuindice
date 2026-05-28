package com.gdavidpb.tuindice.about.presentation.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.gdavidpb.tuindice.about.presentation.route.AboutRoute
import com.gdavidpb.tuindice.about.presentation.utils.LocalShareTextHandler
import com.gdavidpb.tuindice.about.presentation.utils.ShareTextHandler
import com.gdavidpb.tuindice.about.presentation.viewmodel.AboutViewModel
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.aboutNavigation(
	navController: NavHostController,
	onNavigateToBrowser: (title: String, url: String) -> Unit,
	onViewStateChanged: (ViewState) -> Unit
) {
	navigation<AboutDestination.NavGraph>(startDestination = AboutDestination.About) {
		composable<AboutDestination.About> { backStackEntry ->
			val viewModel = koinViewModel<AboutViewModel>(viewModelStoreOwner = backStackEntry)
			val shareTextHandler = koinInject<ShareTextHandler>()
			val viewState by viewModel.state.collectAsStateWithLifecycle()

			navController.CollectCurrentEntryValueWithLifecycle(
				backStackEntry = backStackEntry,
				value = viewState,
				onValue = onViewStateChanged
			)

			CompositionLocalProvider(
				LocalShareTextHandler provides shareTextHandler
			) {
				AboutRoute(
					onNavigateToBrowser = onNavigateToBrowser,
					viewModel = viewModel
				)
			}
		}
	}
}
