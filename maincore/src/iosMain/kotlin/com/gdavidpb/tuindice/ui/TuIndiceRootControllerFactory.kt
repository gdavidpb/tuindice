package com.gdavidpb.tuindice.ui

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.ComposeUIViewController
import com.gdavidpb.tuindice.presentation.route.TuIndiceCoordinatorRoute
import com.gdavidpb.tuindice.ui.screen.TuIndiceScreen
import com.gdavidpb.tuindice.ui.theme.TuIndiceSharedTheme
import kotlinx.coroutines.launch
import platform.UIKit.UIViewController

class TuIndiceRootControllerFactory {
	fun create(): UIViewController {
		return ComposeUIViewController {
			TuIndiceRootContent()
		}
	}
}

@Composable
private fun TuIndiceRootContent() {
	val coroutineScope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }

	TuIndiceSharedTheme {
		TuIndiceCoordinatorRoute(
			onConfirmExitClick = {},
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
				isSwipeBackNavigationEnabled = true,
				snackbarHostState = snackbarHostState,
				onAction = onAction,
				onNavigateTo = onNavigateTo,
				onNavigateBack = onNavigateBack,
				onConfirmExitClick = {},
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
}
