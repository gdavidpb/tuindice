package com.gdavidpb.tuindice.record.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarBannerBehavior
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.contract.CreateSyntheticTerm
import com.gdavidpb.tuindice.record.presentation.route.CreateSyntheticTermRoute
import com.gdavidpb.tuindice.record.presentation.route.RecordRoute
import com.gdavidpb.tuindice.record.presentation.route.toRouteViewState
import com.gdavidpb.tuindice.record.presentation.viewmodel.CreateSyntheticTermViewModel
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.recordNavigation(
	navController: NavHostController,
	onNavigateToUpdatePassword: () -> Unit,
	onTopBarViewModeChangeAvailable: (((RecordViewMode) -> Unit)?) -> Unit,
	showTopBarBanner: (behavior: TopBarBannerBehavior) -> Unit,
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	navigation<RecordDestination.NavGraph>(startDestination = RecordDestination.Record) {
		composable<RecordDestination.Record> { backStackEntry ->
			val viewModel = koinViewModel<RecordViewModel>(viewModelStoreOwner = backStackEntry)
			val viewState by viewModel.state.collectAsStateWithLifecycle()

			navController.CollectCurrentEntryValueWithLifecycle(
				backStackEntry = backStackEntry,
				value = viewState.toRouteViewState(),
				onValue = onViewStateChanged
			)

			RecordRoute(
				onNavigateToUpdatePassword = onNavigateToUpdatePassword,
				onNavigateToCreateSyntheticTerm = {
					navController.navigate(RecordDestination.CreateSyntheticTerm())
				},
				onNavigateToUpdateSyntheticTerm = { termId ->
					navController.navigate(RecordDestination.CreateSyntheticTerm(termId = termId))
				},
				onTopBarViewModeChangeAvailable = onTopBarViewModeChangeAvailable,
				showTopBarBanner = showTopBarBanner,
				showSnackBar = showSnackBar,
				viewModel = viewModel
			)
		}

		composable<RecordDestination.CreateSyntheticTerm> { backStackEntry ->
			val args = backStackEntry.toRoute<RecordDestination.CreateSyntheticTerm>()
			val viewModel = koinViewModel<CreateSyntheticTermViewModel>(viewModelStoreOwner = backStackEntry)
			val viewState by viewModel.state.collectAsStateWithLifecycle()

			navController.CollectCurrentEntryValueWithLifecycle(
				backStackEntry = backStackEntry,
				value = viewState,
				onValue = onViewStateChanged
			)

			CreateSyntheticTermRoute(
				termId = args.termId,
				viewModel = viewModel,
				onBack = { navController.navigateUp() },
				showSnackBar = showSnackBar
			)
		}
	}
}
