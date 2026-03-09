package com.gdavidpb.tuindice.record.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.record.presentation.route.RecordRoute
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.recordNavigation(
	onNavigateToUpdatePassword: () -> Unit,
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	navigation<RecordDestination.NavGraph>(startDestination = RecordDestination.Record) {
		composable<RecordDestination.Record> { backStackEntry ->
			val viewModel = koinViewModel<RecordViewModel>(viewModelStoreOwner = backStackEntry)
			val viewState by viewModel.state.collectAsStateWithLifecycle()

			LaunchedEffect(viewState) {
				onViewStateChanged(viewState)
			}

			RecordRoute(
				onNavigateToUpdatePassword = onNavigateToUpdatePassword,
				showSnackBar = showSnackBar,
				viewModel = viewModel
			)
		}
	}
}
