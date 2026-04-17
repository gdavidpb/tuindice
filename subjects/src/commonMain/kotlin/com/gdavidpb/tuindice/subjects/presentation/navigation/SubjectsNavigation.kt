package com.gdavidpb.tuindice.subjects.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.subjects.presentation.route.SubjectDetailRoute
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.subjectsNavigation(
	navController: NavHostController,
	onViewStateChanged: (ViewState) -> Unit,
	onDismissRequest: () -> Unit
) {
	composable<SubjectsDestination.SubjectDetail> { backStackEntry ->
		val args = backStackEntry.toRoute<SubjectsDestination.SubjectDetail>()
		val viewModel = koinViewModel<com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectDetailViewModel>(
			viewModelStoreOwner = backStackEntry
		)
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		navController.CollectCurrentEntryValueWithLifecycle(
			backStackEntry = backStackEntry,
			value = viewState,
			onValue = onViewStateChanged
		)

		SubjectDetailRoute(
			subjectCode = args.subjectCode,
			viewModel = viewModel,
			onDismissRequest = onDismissRequest
		)
	}
}
