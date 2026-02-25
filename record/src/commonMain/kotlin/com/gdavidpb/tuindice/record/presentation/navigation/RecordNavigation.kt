package com.gdavidpb.tuindice.record.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.route.RecordRoute
import com.gdavidpb.tuindice.record.presentation.viewmodel.RecordViewModel
import org.koin.compose.koinInject

fun NavGraphBuilder.recordNavigation(
	onNavigateToUpdatePassword: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	recordContent: @Composable (
		state: Record.State,
		onRetryClick: () -> Unit,
		onSubjectGradeChange: (
			quarterId: String,
			subjectId: String,
			newGrade: Int,
			isSelected: Boolean
		) -> Unit
	) -> Unit
) {
	navigation<RecordDestination.NavGraph>(startDestination = RecordDestination.Record) {
		composable<RecordDestination.Record> {
			val viewModel = koinInject<RecordViewModel>()

			RecordRoute(
				onNavigateToUpdatePassword = onNavigateToUpdatePassword,
				showSnackBar = showSnackBar,
				viewModel = viewModel,
				content = recordContent
			)
		}
	}
}
