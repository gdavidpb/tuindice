package com.gdavidpb.tuindice.pensum.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.pensum.presentation.model.PensumTopBarActionBus
import com.gdavidpb.tuindice.pensum.presentation.route.PensumRoute
import com.gdavidpb.tuindice.pensum.presentation.viewmodel.PensumViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.pensumNavigation(
	navController: NavHostController,
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	navigation<PensumDestination.NavGraph>(startDestination = PensumDestination.Pensum) {
		composable<PensumDestination.Pensum> { backStackEntry ->
			val viewModel = koinViewModel<PensumViewModel>(viewModelStoreOwner = backStackEntry)
			val topBarActionBus = koinInject<PensumTopBarActionBus>()
			val viewState by viewModel.state.collectAsStateWithLifecycle()

			navController.CollectCurrentEntryValueWithLifecycle(
				backStackEntry = backStackEntry,
				value = viewState,
				onValue = onViewStateChanged
			)

			PensumRoute(
				showSnackBar = showSnackBar,
				topBarActionBus = topBarActionBus,
				viewModel = viewModel
			)
		}
	}
}
