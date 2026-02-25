package com.gdavidpb.tuindice.presentation.route

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.utils.extension.isCurrentDestination
import com.gdavidpb.tuindice.base.utils.extension.viewModel
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.EnrollmentProofDestination
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.login.presentation.navigation.LoginDestination
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.navigation.MainDestination
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import org.koin.compose.koinInject

@Composable
fun TuIndiceCoordinatorRoute(
	onConfirmExitClick: () -> Unit,
	showSnackBar: (SnackBarMessage) -> Unit,
	browserGateway: BrowserRepository = koinInject(),
	deviceInfoGateway: DeviceInfoRepository = koinInject(),
	reviewGateway: ReviewRepository = koinInject(),
	updateGateway: UpdateRepository = koinInject(),
	viewModel: MainViewModel = koinInject(),
	content: @Composable (
		state: Main.State,
		updateState: (Main.State) -> Unit,
		onRetryStartUp: () -> Unit,
		navController: NavHostController,
		onAction: (action: TopBarAction) -> Unit,
		onNavigateTo: (destination: Destination) -> Unit,
		onNavigateBack: () -> Unit,
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
	val lifecycleOwner = LocalLifecycleOwner.current
	val navController = rememberNavController()

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
			reviewGateway.launchReview()
		},
		onRequestUpdateFlow = { action ->
			updateGateway.launchUpdate(action = action)
		},
		viewModel = viewModel
	) { state, updateState ->
		content(
			state,
			updateState,
			viewModel::startUpAction,
			navController,
			{ action ->
				when (action) {
					is TopBarAction.SignOutAction ->
						navController.navigate(LoginDestination.SignOutDialog)

					is TopBarAction.FetchEnrollmentProofAction ->
						navController.navigate(EnrollmentProofDestination.EnrollmentProofDialog)
				}
			},
			{ destination ->
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
			{ navController.navigateUp() },
			deviceInfoGateway.hasCamera(),
			browserGateway::open,
			{
				navController
					.viewModel<SummaryViewModel>()
					?.confirmRemoveProfilePictureAction()
			},
			{
				navController
					.viewModel<SummaryViewModel>()
					?.pickProfilePictureAction()
			},
			{
				navController
					.viewModel<SummaryViewModel>()
					?.takeProfilePictureAction()
			},
			{
				navController
					.viewModel<SummaryViewModel>()
					?.removeProfilePictureAction()
			},
			{ grade ->
				navController
					.viewModel<EvaluationViewModel>()
					?.setGradeAction(grade = grade)
			},
			{ grade ->
				navController
					.viewModel<EvaluationViewModel>()
					?.setMaxGradeAction(grade = grade)
			},
			{ evaluationId, grade ->
				navController
					.viewModel<EvaluationsViewModel>()
					?.setEvaluationGradeAction(
						evaluationId = evaluationId,
						grade = grade
					)
			},
			showSnackBar
		)
	}
}
