package com.gdavidpb.tuindice.subjects.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import com.gdavidpb.tuindice.subjects.presentation.route.SubjectDetailRoute
import org.koin.compose.viewmodel.koinViewModel

fun NavGraphBuilder.subjectsNavigation(
	onDismissRequest: () -> Unit
) {
	dialog<SubjectsDestination.SubjectDetail> { backStackEntry ->
		val args = backStackEntry.toRoute<SubjectsDestination.SubjectDetail>()
		val viewModel = koinViewModel<com.gdavidpb.tuindice.subjects.presentation.viewmodel.SubjectDetailViewModel>(
			viewModelStoreOwner = backStackEntry
		)

		SubjectDetailRoute(
			subjectCode = args.subjectCode,
			viewModel = viewModel,
			onDismissRequest = onDismissRequest
		)
	}
}
