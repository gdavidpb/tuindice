package com.gdavidpb.tuindice.ui.screen

import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import com.gdavidpb.tuindice.about.presentation.contract.About
import com.gdavidpb.tuindice.about.presentation.navigation.aboutNavigation
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.enrollmentproof.presentation.contract.Enrollment
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.enrollmentProofNavigation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationFilter
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluation
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.evaluations.presentation.navigation.evaluationsNavigation
import com.gdavidpb.tuindice.login.presentation.contract.SignIn
import com.gdavidpb.tuindice.login.presentation.contract.SignOut
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.presentation.navigation.LoginDestination
import com.gdavidpb.tuindice.login.presentation.navigation.loginNavigation
import com.gdavidpb.tuindice.presentation.contract.Browser
import com.gdavidpb.tuindice.presentation.navigation.BrowserDestination
import com.gdavidpb.tuindice.presentation.navigation.browserNavigation
import com.gdavidpb.tuindice.presentation.navigation.mainNavigation
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.navigation.recordNavigation
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.summaryNavigation
import com.gdavidpb.tuindice.summary.presentation.route.ProfilePictureActions
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.ui.navigation.PlatformBackGesture
import com.gdavidpb.tuindice.ui.navigation.edgeSwipeBackNavigation

@Composable
fun TuIndiceNavHost(
	navController: NavHostController,
	startDestination: Destination,
	modifier: Modifier = Modifier.fillMaxSize(),
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
	signInContent: @Composable (
		state: SignIn.State,
		onUsbIdChange: (String) -> Unit,
		onPasswordChange: (String) -> Unit,
		onSignInClick: (usbId: String, password: String) -> Unit,
		onTermsAndConditionsClick: () -> Unit,
		onPrivacyPolicyClick: () -> Unit
	) -> Unit,
	signOutDialogContent: @Composable (
		state: SignOut.State,
		onConfirmClick: () -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit,
	updatePasswordDialogContent: @Composable (
		state: UpdatePassword.State,
		onPasswordChange: (String) -> Unit,
		onConfirmClick: (password: String) -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit,
	profilePictureActionsFactory: @Composable (viewModel: SummaryViewModel) -> ProfilePictureActions,
	summaryContent: @Composable (
		state: Summary.State,
		onRetryClick: () -> Unit,
		onEditProfilePictureClick: () -> Unit
	) -> Unit,
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
	recordContent: @Composable (
		state: Record.State,
		onRetryClick: () -> Unit,
		onSubjectGradeChange: (
			quarterId: String,
			subjectId: String,
			newGrade: Int,
			isSelected: Boolean
		) -> Unit
	) -> Unit,
	evaluationsContent: @Composable (
		state: Evaluations.State,
		onAddEvaluationClick: () -> Unit,
		onEvaluationClick: (evaluationId: String) -> Unit,
		onEvaluationEdit: (evaluationId: String) -> Unit,
		onEvaluationDelete: (evaluationId: String) -> Unit,
		onFilterCheckedChange: (filter: EvaluationFilter, isChecked: Boolean) -> Unit,
		onClearFiltersClick: () -> Unit,
		onRetryClick: () -> Unit
	) -> Unit,
	evaluationContent: @Composable (
		state: Evaluation.State,
		onSubjectChange: (subject: Subject) -> Unit,
		onTypeChange: (type: EvaluationType) -> Unit,
		onDateChange: (date: Long?) -> Unit,
		onGradeClick: (grade: Double?, maxGrade: Double?) -> Unit,
		onMaxGradeClick: (maxGrade: Double?) -> Unit,
		onDoneClick: (
			subject: Subject?,
			type: EvaluationType?,
			date: Long?,
			grade: Double?,
			maxGrade: Double?
		) -> Unit,
		onRetryClick: () -> Unit
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
	aboutContent: @Composable (
		state: About.State,
		onCreativeCommonsClick: () -> Unit,
		onXClick: () -> Unit,
		onGithubClick: () -> Unit,
		onKotlinClick: () -> Unit,
		onComposeClick: () -> Unit,
		onFirebaseClick: () -> Unit,
		onKoinClick: () -> Unit,
		onKtorClick: () -> Unit,
		onDstClick: () -> Unit,
		onTermsAndConditionsClick: () -> Unit,
		onPrivacyPolicyClick: () -> Unit,
		onShareAppClick: () -> Unit,
		onRateOnPlayStoreClick: () -> Unit,
		onContactDeveloperClick: () -> Unit,
		onReportBugClick: () -> Unit
	) -> Unit,
	enrollmentProofDialogContent: @Composable (
		state: Enrollment.State,
		onDismissRequest: () -> Unit
	) -> Unit,
	browserContent: @Composable (
		state: Browser.State,
		onPageStarted: () -> Unit,
		onPageFinished: () -> Unit,
		onExternalResourceClick: (url: String) -> Unit
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
			enabled = PlatformBackGesture.isEnabled && canNavigateBack,
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
			showSnackBar = showSnackBar,
			signInContent = signInContent,
			signOutDialogContent = signOutDialogContent,
			updatePasswordDialogContent = updatePasswordDialogContent
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
			summaryContent = summaryContent,
			removeProfilePictureConfirmationDialogContent = removeProfilePictureConfirmationDialogContent,
			profilePictureSettingsDialogContent = profilePictureSettingsDialogContent
		)

		recordNavigation(
			onNavigateToUpdatePassword = {
				navController.navigate(LoginDestination.UpdatePasswordDialog)
			},
			showSnackBar = showSnackBar,
			recordContent = recordContent
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
			evaluationsContent = evaluationsContent,
			evaluationContent = evaluationContent,
			gradePickerDialogContent = gradePickerDialogContent,
			maxGradePickerDialogContent = maxGradePickerDialogContent,
			evaluationGradePickerDialogContent = evaluationGradePickerDialogContent
		)

		aboutNavigation(
			onNavigateToBrowser = { title, url ->
				navController.navigate(BrowserDestination.Browser(title = title, url = url))
			},
			aboutContent = aboutContent
		)

		enrollmentProofNavigation(
			navigateToUpdatePassword = {
				navController.navigate(LoginDestination.UpdatePasswordDialog)
			},
			onDismissRequest = { navController.popBackStack() },
			showSnackBar = showSnackBar,
			enrollmentProofDialogContent = enrollmentProofDialogContent
		)

		browserNavigation(
			onNavigateToExternalResourceDialog = { url ->
				navController.navigate(BrowserDestination.ExternalResourceDialog(url = url))
			},
			onNavigateToExternalResource = onNavigateToExternalResource,
			onDismissRequest = { navController.navigateUp() },
			browserContent = browserContent,
			externalResourceDialogContent = externalResourceDialogContent
		)
	}
}
