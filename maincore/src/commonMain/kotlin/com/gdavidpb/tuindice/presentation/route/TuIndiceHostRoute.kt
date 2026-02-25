package com.gdavidpb.tuindice.presentation.route

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavHostController
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.launch

@Composable
fun TuIndiceHostRoute(
	onConfirmExitClick: () -> Unit,
	content: @Composable (
		state: Main.State,
		updateState: (Main.State) -> Unit,
		onRetryStartUp: () -> Unit,
		navController: NavHostController,
		snackbarHostState: SnackbarHostState,
		onAction: (action: TopBarAction) -> Unit,
		onNavigateTo: (destination: Destination) -> Unit,
		onNavigateBack: () -> Unit,
		onConfirmExitClick: () -> Unit,
		isCameraAvailable: Boolean,
		onNavigateToExternalResource: (url: String) -> Unit,
		onConfirmRemoveProfilePicture: () -> Unit,
		onPickProfilePicture: () -> Unit,
		onTakeProfilePicture: () -> Unit,
		onRemoveProfilePicture: () -> Unit,
		onSetGrade: (grade: Double) -> Unit,
		onSetMaxGrade: (grade: Double) -> Unit,
		onSetEvaluationGrade: (evaluationId: String, grade: Double) -> Unit,
		showSnackBar: (message: SnackBarMessage) -> Unit
	) -> Unit
) {
	val coroutineScope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }

	TuIndiceCoordinatorRoute(
		onConfirmExitClick = onConfirmExitClick,
		showSnackBar = { message ->
			coroutineScope.launch {
				snackbarHostState.currentSnackbarData?.dismiss()

				val snackBarResult = snackbarHostState.showSnackbar(
					message = message.message,
					actionLabel = message.actionLabel,
					duration = if (message.actionLabel == null)
						SnackbarDuration.Short
					else
						SnackbarDuration.Long
				)

				when (snackBarResult) {
					SnackbarResult.ActionPerformed -> message.onAction?.invoke()
					SnackbarResult.Dismissed -> message.onDismissed?.invoke()
				}
			}
		}
	) { state, updateState, onRetryStartUp, navController, onAction, onNavigateTo, onNavigateBack, isCameraAvailable, onNavigateToExternalResource, onConfirmRemoveProfilePicture, onPickProfilePicture, onTakeProfilePicture, onRemoveProfilePicture, onSetGrade, onSetMaxGrade, onSetEvaluationGrade, showSnackBar ->
		content(
			state,
			updateState,
			onRetryStartUp,
			navController,
			snackbarHostState,
			onAction,
			onNavigateTo,
			onNavigateBack,
			onConfirmExitClick,
			isCameraAvailable,
			onNavigateToExternalResource,
			onConfirmRemoveProfilePicture,
			onPickProfilePicture,
			onTakeProfilePicture,
			onRemoveProfilePicture,
			onSetGrade,
			onSetMaxGrade,
			onSetEvaluationGrade,
			showSnackBar
		)
	}
}
