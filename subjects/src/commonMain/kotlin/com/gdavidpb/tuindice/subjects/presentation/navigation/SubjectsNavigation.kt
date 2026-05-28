package com.gdavidpb.tuindice.subjects.presentation.navigation

import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.utils.extension.CollectCurrentEntryValueWithLifecycle
import com.gdavidpb.tuindice.subjects.presentation.contract.SubjectDetail
import com.gdavidpb.tuindice.subjects.presentation.route.SubjectDetailRoute
import com.gdavidpb.tuindice.subjects.presentation.route.SubjectSearchRoute
import org.koin.compose.viewmodel.koinViewModel
import tuindice.subjects.generated.resources.Res
import tuindice.subjects.generated.resources.top_bar_subject_detail

fun NavGraphBuilder.subjectsNavigation(
	navController: NavHostController,
	onViewStateChanged: (ViewState) -> Unit,
	onDismissRequest: () -> Unit
) {
	composable<SubjectsDestination.SubjectSearch> { backStackEntry ->
		val viewModel = koinViewModel<com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectSearchViewModel>(
			viewModelStoreOwner = backStackEntry
		)
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		navController.CollectCurrentEntryValueWithLifecycle(
			backStackEntry = backStackEntry,
			value = viewState,
			onValue = onViewStateChanged
		)

		SubjectSearchRoute(
			viewModel = viewModel,
			onSubjectClick = { subjectCode ->
				navController.navigate(SubjectsDestination.SubjectDetail(subjectCode = subjectCode))
			}
		)
	}

	composable<SubjectsDestination.SubjectDetail> { backStackEntry ->
		val args = backStackEntry.toRoute<SubjectsDestination.SubjectDetail>()
		val viewModel = koinViewModel<com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectDetailViewModel>(
			viewModelStoreOwner = backStackEntry
		)
		val viewState by viewModel.state.collectAsStateWithLifecycle()

		navController.CollectCurrentEntryValueWithLifecycle(
			backStackEntry = backStackEntry,
			value = viewState.resolveNavigationViewState(subjectCode = args.subjectCode),
			onValue = onViewStateChanged
		)

		SubjectDetailRoute(
			subjectCode = args.subjectCode,
			viewModel = viewModel,
			onDismissRequest = onDismissRequest
		)
	}
}

private fun SubjectDetail.State.resolveNavigationViewState(subjectCode: String): ViewState {
	return when (this) {
		SubjectDetail.State.Idle,
		SubjectDetail.State.Loading ->
			object : ViewState(
				topBarTitle = UiText.Resource(
					resource = Res.string.top_bar_subject_detail,
					args = listOf(subjectCode)
				),
				isTopBarVisible = true
			) {}

		is SubjectDetail.State.Content,
		is SubjectDetail.State.Failed,
		is SubjectDetail.State.Unavailable,
		-> this
	}
}
