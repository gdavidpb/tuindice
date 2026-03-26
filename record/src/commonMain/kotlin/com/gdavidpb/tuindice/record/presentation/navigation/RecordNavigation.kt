package com.gdavidpb.tuindice.record.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
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

			RecordRoute(
				onNavigateToUpdatePassword = onNavigateToUpdatePassword,
				onViewStateChanged = onViewStateChanged,
				showSnackBar = showSnackBar,
				viewModel = viewModel
			)
		}
	}
}
