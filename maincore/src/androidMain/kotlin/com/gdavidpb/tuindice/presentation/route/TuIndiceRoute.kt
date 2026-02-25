package com.gdavidpb.tuindice.presentation.route

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.ui.screen.TuIndiceScreen

@Composable
fun TuIndiceRoute(
	onConfirmExitClick: () -> Unit
) {
	TuIndiceHostRoute(
		onConfirmExitClick = onConfirmExitClick
	) { state, updateState, onRetryStartUp, navController, snackbarHostState, onAction, onNavigateTo, onNavigateBack, confirmExitClick, isCameraAvailable, onNavigateToExternalResource, onConfirmRemoveProfilePicture, onPickProfilePicture, onTakeProfilePicture, onRemoveProfilePicture, onSetGrade, onSetMaxGrade, onSetEvaluationGrade, showSnackBar ->
		TuIndiceScreen(
			state = state,
			updateState = updateState,
			onRetryStartUp = onRetryStartUp,
			navController = navController,
			snackbarHostState = snackbarHostState,
			onAction = onAction,
			onNavigateTo = onNavigateTo,
			onNavigateBack = onNavigateBack,
			onConfirmExitClick = confirmExitClick,
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
