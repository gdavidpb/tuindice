package com.gdavidpb.tuindice.presentation.route

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.rememberNavController
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.utils.extension.isCurrentDestination
import com.gdavidpb.tuindice.base.utils.extension.viewModel
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.EnrollmentProofDestination
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.login.presentation.navigation.LoginDestination
import com.gdavidpb.tuindice.presentation.navigation.MainDestination
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.ui.screen.TuIndiceScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun TuIndiceAppHostRoute(
	onConfirmExitClick: () -> Unit,
	isSwipeBackNavigationEnabled: Boolean = false,
	browserRepository: BrowserRepository = koinInject(),
	deviceInfoRepository: DeviceInfoRepository = koinInject(),
	reviewRepository: ReviewRepository = koinInject(),
	updateRepository: UpdateRepository = koinInject(),
	viewModel: MainViewModel = koinInject()
) {
	val lifecycleOwner = LocalLifecycleOwner.current
	val navController = rememberNavController()
	val coroutineScope = rememberCoroutineScope()
	val snackbarHostState = remember { SnackbarHostState() }

	val showSnackBar: (SnackBarMessage) -> Unit = { message ->
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

	LaunchedEffect(Unit) {
		viewModel.requestReviewAction()

		lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.RESUMED) {
			viewModel.checkUpdateAction()
		}
	}

	MainRoute(
		onNavigateToGooglePlayServicesUnavailableDialog = {
			navController.navigate(MainDestination.GooglePlayServicesUnavailableDialog)
		},
		onRequestReviewFlow = {
			reviewRepository.launchReview()
		},
		onRequestUpdateFlow = { action ->
			updateRepository.launchUpdate(action = action)
		},
		viewModel = viewModel
	) { state, updateState ->
		TuIndiceScreen(
			state = state,
			updateState = updateState,
			onRetryStartUp = viewModel::startUpAction,
			navController = navController,
			isSwipeBackNavigationEnabled = isSwipeBackNavigationEnabled,
			snackbarHostState = snackbarHostState,
			onAction = { action ->
				when (action) {
					is TopBarAction.SignOutAction ->
						navController.navigate(LoginDestination.SignOutDialog)

					is TopBarAction.FetchEnrollmentProofAction ->
						navController.navigate(EnrollmentProofDestination.EnrollmentProofDialog)
				}
			},
			onNavigateTo = { destination ->
				val currentDestination = navController.currentDestination?.parent?.route
				val isNewDestination = !navController.isCurrentDestination(destination)

				if (isNewDestination) {
					viewModel.setLastDestinationAction(destination)

					navController.navigate(destination) {
						launchSingleTop = true

						if (currentDestination != null)
							popUpTo(currentDestination) {
								inclusive = true
							}
					}
				}
			},
			onNavigateBack = { navController.navigateUp() },
			onConfirmExitClick = onConfirmExitClick,
			isCameraAvailable = deviceInfoRepository.hasCamera(),
			onNavigateToExternalResource = browserRepository::open,
			onConfirmRemoveProfilePicture = {
				navController
					.viewModel<SummaryViewModel>()
					?.confirmRemoveProfilePictureAction()
			},
			onPickProfilePicture = {
				navController
					.viewModel<SummaryViewModel>()
					?.pickProfilePictureAction()
			},
			onTakeProfilePicture = {
				navController
					.viewModel<SummaryViewModel>()
					?.takeProfilePictureAction()
			},
			onRemoveProfilePicture = {
				navController
					.viewModel<SummaryViewModel>()
					?.removeProfilePictureAction()
			},
			onSetGrade = { grade ->
				navController
					.viewModel<EvaluationViewModel>()
					?.setGradeAction(grade = grade)
			},
			onSetMaxGrade = { grade ->
				navController
					.viewModel<EvaluationViewModel>()
					?.setMaxGradeAction(grade = grade)
			},
			onSetEvaluationGrade = { evaluationId, grade ->
				navController
					.viewModel<EvaluationsViewModel>()
					?.setEvaluationGradeAction(
						evaluationId = evaluationId,
						grade = grade
					)
			},
			showSnackBar = showSnackBar
		)
	}
}
