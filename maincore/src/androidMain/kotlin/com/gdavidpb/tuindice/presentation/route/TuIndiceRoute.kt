package com.gdavidpb.tuindice.presentation.route

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.gdavidpb.tuindice.ui.screen.TuIndiceScreen
import kotlinx.coroutines.launch

@Composable
fun TuIndiceRoute(
	onConfirmExitClick: () -> Unit
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
		TuIndiceScreen(
			state = state,
			updateState = updateState,
			onRetryStartUp = onRetryStartUp,
			navController = navController,
			snackbarHostState = snackbarHostState,
			onAction = onAction,
			onNavigateTo = onNavigateTo,
			onNavigateBack = onNavigateBack,
			onConfirmExitClick = onConfirmExitClick,
			isCameraAvailable = isCameraAvailable,
			onNavigateToExternalResource = onNavigateToExternalResource,
			onConfirmRemoveProfilePicture = onConfirmRemoveProfilePicture,
			onPickProfilePicture = onPickProfilePicture,
			onTakeProfilePicture = onTakeProfilePicture,
			onRemoveProfilePicture = onRemoveProfilePicture,
			onSetGrade = onSetGrade,
			onSetMaxGrade = onSetMaxGrade,
			onSetEvaluationGrade = onSetEvaluationGrade,
			showSnackBar = showSnackBar
		)
	}
}
