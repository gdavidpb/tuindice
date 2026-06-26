package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.gdavidpb.tuindice.about.presentation.navigation.aboutNavigation
import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.auth.presentation.navigation.authNavigation
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarBannerBehavior
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.utils.extension.canNavigateBackFromCurrentDestination
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.EnrollmentProofDestination
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.enrollmentProofNavigation
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.evaluations.presentation.navigation.evaluationsNavigation
import com.gdavidpb.tuindice.presentation.navigation.BrowserDestination
import com.gdavidpb.tuindice.presentation.navigation.browserNavigation
import com.gdavidpb.tuindice.presentation.navigation.mainNavigation
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.navigation.recordNavigation
import com.gdavidpb.tuindice.subjects.presentation.navigation.SubjectsDestination
import com.gdavidpb.tuindice.subjects.presentation.navigation.subjectsNavigation
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.summaryNavigation
import com.gdavidpb.tuindice.pensum.presentation.navigation.pensumNavigation
import com.gdavidpb.tuindice.ui.MaincoreUiTags
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
	onOutdatedAppDetected: () -> Unit = {},
	onUpdatePasswordDismissRequest: () -> Unit = {},
	onRecordViewModeChangeAvailable: (((RecordViewMode) -> Unit)?) -> Unit,
	onRecordTermSelectionAvailable: ((() -> Unit)?) -> Unit,
	showTopBarBanner: (behavior: TopBarBannerBehavior) -> Unit,
	onViewStateChanged: (ViewState) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit,
	dismissSnackBar: () -> Unit = {}
) {
	val canNavigateBack = navController.canNavigateBackFromCurrentDestination()

	NavHost(
			navController = navController,
			startDestination = startDestination,
			modifier = modifier.edgeSwipeBackNavigation(
				enabled = isSwipeBackNavigationEnabled && canNavigateBack,
				onBack = { navController.navigateUp() }
			)
				.background(MaterialTheme.colorScheme.background)
				.testTag(MaincoreUiTags.TuIndiceNavHost)
		) {
		mainNavigation(
			onConfirmExitClick = onConfirmExitClick,
			onDismissRequest = { navController.navigateUp() }
		)

		authNavigation(
			navController = navController,
			onNavigateToSignIn = {
				navController.navigate(AuthDestination.NavGraph) {
					launchSingleTop = true

					popUpTo(SummaryDestination.NavGraph) {
						inclusive = true
					}
				}
			},
			onNavigateToSummary = {
				navController.navigate(SummaryDestination.NavGraph) {
					popUpTo<AuthDestination.NavGraph> {
						inclusive = true
					}
				}
			},
			onNavigateToBrowser = { title, url ->
				navController.navigate(BrowserDestination.Browser(title = title, url = url))
			},
			onOutdatedAppDetected = onOutdatedAppDetected,
			onDismissRequest = { navController.navigateUp() },
			onUpdatePasswordDismissRequest = onUpdatePasswordDismissRequest,
			onViewStateChanged = onViewStateChanged,
			showSnackBar = showSnackBar,
			dismissSnackBar = dismissSnackBar
		)

		summaryNavigation(
			navController = navController,
			isCameraAvailable = isCameraAvailable,
			onNavigateToUpdatePassword = {
				navController.navigate(AuthDestination.UpdatePasswordDialog)
			},
			showSnackBar = showSnackBar,
			onViewStateChanged = onViewStateChanged
		)

		recordNavigation(
			navController = navController,
			onNavigateToUpdatePassword = {
				navController.navigate(AuthDestination.UpdatePasswordDialog)
			},
			onNavigateToSubjectDetail = { subjectCode ->
				navController.navigate(SubjectsDestination.SubjectDetail(subjectCode = subjectCode))
			},
			onTopBarViewModeChangeAvailable = onRecordViewModeChangeAvailable,
			onTopBarTermSelectionAvailable = onRecordTermSelectionAvailable,
			onNavigateToEnrollmentProof = {
				navController.navigate(EnrollmentProofDestination.EnrollmentProofDialog)
			},
			showTopBarBanner = showTopBarBanner,
			onViewStateChanged = onViewStateChanged,
			showSnackBar = showSnackBar
		)

		evaluationsNavigation(
			navController = navController,
			onNavigateToAddEvaluation = {
				navController.navigate(EvaluationsDestination.Evaluation(evaluationId = null))
			},
			onNavigateToEvaluation = { evaluationId ->
				navController.navigate(EvaluationsDestination.Evaluation(evaluationId = evaluationId))
			},
				onNavigateToEvaluationGradePickerDialog = { evaluationId, evaluationName, subjectCode, grade, maxGrade ->
					navController.navigate(
						EvaluationsDestination.EvaluationGradePickerDialog(
							evaluationId = evaluationId,
							evaluationName = evaluationName,
							subjectCode = subjectCode,
							grade = grade,
							maxGrade = maxGrade
						)
					)
				},
				onNavigateToGradePickerDialog = { evaluationName, subjectCode, grade, maxGrade ->
					navController.navigate(
						EvaluationsDestination.GradePickerDialog(
							evaluationName = evaluationName,
							subjectCode = subjectCode,
							grade = grade,
							maxGrade = maxGrade
						)
					)
				},
				onNavigateToMaxGradePickerDialog = { evaluationName, subjectCode, maxGrade ->
					navController.navigate(
						EvaluationsDestination.MaxGradePickerDialog(
							evaluationName = evaluationName,
							subjectCode = subjectCode,
							grade = maxGrade
						)
					)
				},
			onNavigateToEvaluations = { navController.navigate(EvaluationsDestination.Evaluations) },
			onViewStateChanged = onViewStateChanged,
			showSnackBar = showSnackBar
		)

		aboutNavigation(
			navController = navController,
			onNavigateToBrowser = { title, url ->
				navController.navigate(BrowserDestination.Browser(title = title, url = url))
			},
			onViewStateChanged = onViewStateChanged
		)

		enrollmentProofNavigation(
			navigateToUpdatePassword = {
				navController.navigate(AuthDestination.UpdatePasswordDialog)
			},
			onDismissRequest = { navController.navigateUp() },
			showSnackBar = showSnackBar
		)

		subjectsNavigation(
			navController = navController,
			onViewStateChanged = onViewStateChanged,
			onDismissRequest = { navController.navigateUp() }
		)

		pensumNavigation(
			navController = navController,
			onViewStateChanged = onViewStateChanged,
			onNavigateToSubjectDetail = { subjectCode ->
				navController.navigate(SubjectsDestination.SubjectDetail(subjectCode = subjectCode))
			}
		)

		browserNavigation(
			navController = navController,
			onNavigateToExternalResourceDialog = { url ->
				navController.navigate(BrowserDestination.ExternalResourceDialog(url = url))
			},
			onNavigateToExternalResource = onNavigateToExternalResource,
			onDismissRequest = { navController.navigateUp() },
			onViewStateChanged = onViewStateChanged
		)
	}
}
