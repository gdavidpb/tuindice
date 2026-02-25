package com.gdavidpb.tuindice.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gdavidpb.tuindice.about.presentation.navigation.aboutNavigation
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.enrollmentProofNavigation
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.evaluations.presentation.navigation.evaluationsNavigation
import com.gdavidpb.tuindice.login.presentation.navigation.LoginDestination
import com.gdavidpb.tuindice.login.presentation.navigation.loginNavigation
import com.gdavidpb.tuindice.presentation.navigation.BrowserDestination
import com.gdavidpb.tuindice.presentation.navigation.browserNavigation
import com.gdavidpb.tuindice.presentation.navigation.mainNavigation
import com.gdavidpb.tuindice.record.presentation.navigation.recordNavigation
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.summaryNavigation
import com.gdavidpb.tuindice.summary.presentation.route.ProfilePictureActions
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.ui.navigation.edgeSwipeBackNavigation

@Composable
fun TuIndiceNavHost(
	navController: NavHostController,
	startDestination: Destination,
	modifier: Modifier = Modifier.fillMaxSize(),
	isSwipeBackNavigationEnabled: Boolean = false,
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
	showSnackBar: (message: SnackBarMessage) -> Unit,
	googlePlayServicesDialogContent: @Composable (
		onConfirmExitClick: () -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit,
	profilePictureActionsFactory: @Composable (viewModel: SummaryViewModel) -> ProfilePictureActions,
	removeProfilePictureConfirmationDialogContent: @Composable (
		onConfirmClick: () -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit,
	profilePictureSettingsDialogContent: @Composable (
		showRemove: Boolean,
		isCameraAvailable: Boolean,
		onPickPictureClick: () -> Unit,
		onTakePictureClick: () -> Unit,
		onRemovePictureClick: () -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit,
	gradePickerDialogContent: @Composable (
		selectedGrade: Double?,
		maxGrade: Double?,
		onGradeChange: (grade: Double) -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit,
	maxGradePickerDialogContent: @Composable (
		selectedGrade: Double?,
		onGradeChange: (grade: Double) -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit,
	evaluationGradePickerDialogContent: @Composable (
		selectedGrade: Double?,
		maxGrade: Double,
		onGradeChange: (grade: Double) -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit,
	externalResourceDialogContent: @Composable (
		url: String,
		onConfirmClick: (url: String) -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit
) {
	val currentBackStackEntry = navController.currentBackStackEntryAsState().value
	val canNavigateBack = currentBackStackEntry != null && navController.previousBackStackEntry != null

	NavHost(
		navController = navController,
		startDestination = startDestination,
		modifier = modifier.edgeSwipeBackNavigation(
			enabled = isSwipeBackNavigationEnabled && canNavigateBack,
			onBack = { navController.navigateUp() }
		)
	) {
		mainNavigation(
			onConfirmExitClick = onConfirmExitClick,
			onDismissRequest = { navController.navigateUp() },
			content = googlePlayServicesDialogContent
		)

		loginNavigation(
			onNavigateToSignIn = {
				navController.navigate(LoginDestination.NavGraph) {
					launchSingleTop = true

					popUpTo(SummaryDestination.NavGraph) {
						inclusive = true
					}
				}
			},
			onNavigateToSummary = {
				navController.navigate(SummaryDestination.NavGraph) {
					popUpTo<LoginDestination.NavGraph> {
						inclusive = true
					}
				}
			},
			onNavigateToBrowser = { title, url ->
				navController.navigate(BrowserDestination.Browser(title = title, url = url))
			},
			onDismissRequest = { navController.navigateUp() },
			showSnackBar = showSnackBar
		)

		summaryNavigation(
			isCameraAvailable = isCameraAvailable,
			onNavigateToProfilePictureSettingsDialog = { showRemove ->
				navController.navigate(SummaryDestination.ProfilePictureSettingsDialog(showRemove = showRemove))
			},
			onNavigateToUpdatePassword = {
				navController.navigate(LoginDestination.UpdatePasswordDialog)
			},
			onNavigateToRemoveProfilePictureConfirmationDialog = {
				navController.navigate(SummaryDestination.RemoveProfilePictureConfirmationDialog)
			},
			onDismissRequest = { navController.navigateUp() },
			onConfirmRemoveProfilePicture = onConfirmRemoveProfilePicture,
			onPickProfilePicture = onPickProfilePicture,
			onTakePicture = onTakeProfilePicture,
			onRemoveProfilePicture = onRemoveProfilePicture,
			showSnackBar = showSnackBar,
			profilePictureActionsFactory = profilePictureActionsFactory,
			removeProfilePictureConfirmationDialogContent = removeProfilePictureConfirmationDialogContent,
			profilePictureSettingsDialogContent = profilePictureSettingsDialogContent
		)

		recordNavigation(
			onNavigateToUpdatePassword = {
				navController.navigate(LoginDestination.UpdatePasswordDialog)
			},
			showSnackBar = showSnackBar
		)

		evaluationsNavigation(
			onNavigateToAddEvaluation = {
				navController.navigate(EvaluationsDestination.Evaluation(evaluationId = null))
			},
			onNavigateToEvaluation = { evaluationId ->
				navController.navigate(EvaluationsDestination.Evaluation(evaluationId = evaluationId))
			},
			onNavigateToEvaluationGradePickerDialog = { evaluationId, grade, maxGrade ->
				navController.navigate(
					EvaluationsDestination.EvaluationGradePickerDialog(
						evaluationId = evaluationId,
						grade = grade,
						maxGrade = maxGrade
					)
				)
			},
			onNavigateToGradePickerDialog = { grade, maxGrade ->
				navController.navigate(
					EvaluationsDestination.GradePickerDialog(
						grade = grade,
						maxGrade = maxGrade
					)
				)
			},
			onNavigateToMaxGradePickerDialog = { maxGrade ->
				navController.navigate(EvaluationsDestination.MaxGradePickerDialog(grade = maxGrade))
			},
			onNavigateToEvaluations = { navController.navigate(EvaluationsDestination.Evaluations) },
			onSetGrade = onSetGrade,
			onSetMaxGrade = onSetMaxGrade,
			onSetEvaluationGrade = onSetEvaluationGrade,
			onDismissRequest = { navController.navigateUp() },
			showSnackBar = showSnackBar,
			gradePickerDialogContent = gradePickerDialogContent,
			maxGradePickerDialogContent = maxGradePickerDialogContent,
			evaluationGradePickerDialogContent = evaluationGradePickerDialogContent
		)

		aboutNavigation(
			onNavigateToBrowser = { title, url ->
				navController.navigate(BrowserDestination.Browser(title = title, url = url))
			}
		)

		enrollmentProofNavigation(
			navigateToUpdatePassword = {
				navController.navigate(LoginDestination.UpdatePasswordDialog)
			},
			onDismissRequest = { navController.popBackStack() },
			showSnackBar = showSnackBar
		)

		browserNavigation(
			onNavigateToExternalResourceDialog = { url ->
				navController.navigate(BrowserDestination.ExternalResourceDialog(url = url))
			},
			onNavigateToExternalResource = onNavigateToExternalResource,
			onDismissRequest = { navController.navigateUp() },
			externalResourceDialogContent = externalResourceDialogContent
		)
	}
}
