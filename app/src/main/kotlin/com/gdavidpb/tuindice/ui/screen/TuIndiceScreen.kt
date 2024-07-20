package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.navigation.BottomSheetNavigator
import androidx.compose.material.navigation.ModalBottomSheetLayout
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.about.presentation.navigation.aboutNavigation
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.ui.view.TopAppBarActionsView
import com.gdavidpb.tuindice.base.ui.view.TopAppBarAnimatedTitleView
import com.gdavidpb.tuindice.base.utils.extension.browse
import com.gdavidpb.tuindice.base.utils.extension.findActivity
import com.gdavidpb.tuindice.base.utils.extension.viewModel
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.enrollmentProofFetchNavigation
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.evaluations.presentation.navigation.evaluationsNavigation
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.login.presentation.navigation.LoginDestination
import com.gdavidpb.tuindice.login.presentation.navigation.loginNavigation
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.navigation.BrowserDestination
import com.gdavidpb.tuindice.presentation.navigation.browserNavigation
import com.gdavidpb.tuindice.presentation.navigation.mainNavigation
import com.gdavidpb.tuindice.record.presentation.navigation.recordNavigation
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.summaryNavigation
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuIndiceScreen(
	state: Main.State,
	updateState: (Main.State) -> Unit,
	navController: NavHostController,
	bottomSheetNavigator: BottomSheetNavigator,
	snackbarHostState: SnackbarHostState,
	onAction: (action: TopBarAction) -> Unit,
	onNavigateTo: (destination: Destination) -> Unit,
	onNavigateBack: () -> Unit,
	onSetDestinationScreen: (destination: Destination) -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	if (state !is Main.State.Content) return

	/* TODO
	LaunchedEffect(navController) {
		navController
			.currentBackStackEntryFlow
			.mapDestination()
			.collect { destination ->
				if (destination.isBottomDestination)
					onSetDestinationScreen(destination)

				updateState(
					state.copy(
						title = destination.title,
						currentDestination = destination,
						topBarConfig = destination.topBarConfig
					)
				)
			}
	}
	 */

	Scaffold(
		snackbarHost = { SnackbarHost(snackbarHostState) },
		topBar = {
			TopAppBar(
				title = {
					TopAppBarAnimatedTitleView(
						title = state.title
					)
				},
				actions = {
					TopAppBarActionsView(
						topBarConfig = state.topBarConfig,
						onAction = onAction
					)
				},
				navigationIcon = {
					if (!state.currentDestination.isTopDestination)
						IconButton(onClick = onNavigateBack) {
							Icon(
								imageVector = Icons.AutoMirrored.Filled.ArrowBack,
								contentDescription = null
							)
						}
				}
			)
		},
		bottomBar = {
			if (state.currentDestination.isBottomDestination) {
				NavigationBar(
					modifier = Modifier.height(dimensionResource(id = R.dimen.dp_48)),
					containerColor = MaterialTheme.colorScheme.onSecondary
				) {
					listOf(
						Destination.Record,
						Destination.Evaluations,
						Destination.About
					).forEach { destination ->
						val bottomBarConfig = destination.bottomBarConfig

						requireNotNull(bottomBarConfig)

						val isNavigationBarItemSelected =
							(destination == state.currentDestination)

						val navigationBarItemIcon =
							if (isNavigationBarItemSelected)
								bottomBarConfig.selectedIcon
							else
								bottomBarConfig.unselectedIcon

						NavigationBarItem(
							icon = {
								Icon(
									imageVector = navigationBarItemIcon,
									contentDescription = null
								)
							},
							colors = NavigationBarItemDefaults.colors(
								indicatorColor = MaterialTheme.colorScheme.secondaryContainer
							),
							selected = isNavigationBarItemSelected,
							onClick = { onNavigateTo(destination) }
						)
					}
				}
			}
		}
	) { innerPadding ->
		ModalBottomSheetLayout(bottomSheetNavigator) {
			val context = LocalContext.current

			NavHost(
				navController = navController,
				startDestination = state.startDestination,
				modifier = Modifier.padding(innerPadding)
			) {
				mainNavigation(
					onConfirmExitClick = {
						context.findActivity().finish()
					},
					onDismissRequest = {
						navController.navigateUp()
					}
				)

				loginNavigation(
					onNavigateToSignIn = {
						navController.navigate(LoginDestination.NavGraph)
					},
					onNavigateToSummary = {
						navController.navigate(SummaryDestination.NavGraph)
					},
					onNavigateToBrowser = { title, url ->
						navController.navigate(
							BrowserDestination.Browser(
								url = url
							)
						)
					},
					onDismissRequest = {
						navController.navigateUp()
					},
					showSnackBar = showSnackBar
				)

				summaryNavigation(
					onNavigateToProfilePictureSettingsDialog = { showRemove ->
						navController.navigate(
							SummaryDestination.ProfilePictureSettingsDialog(
								showRemove = showRemove
							)
						)
					},
					onNavigateToUpdatePassword = {
						navController.navigate(
							LoginDestination.UpdatePasswordDialog
						)
					},
					onNavigateToRemoveProfilePictureConfirmationDialog = {
						navController.navigate(
							SummaryDestination.RemoveProfilePictureConfirmationDialog
						)
					},
					onDismissRequest = {
						navController.navigateUp()
					},
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
					onTakePicture = {
						navController
							.viewModel<SummaryViewModel>()
							?.takeProfilePictureAction()
					},
					onRemoveProfilePicture = {
						navController
							.viewModel<SummaryViewModel>()
							?.removeProfilePictureAction()
					},
					showSnackBar = showSnackBar
				)

				recordNavigation(
					onNavigateToUpdatePassword = {
						navController.navigate(
							LoginDestination.UpdatePasswordDialog
						)
					},
					showSnackBar = showSnackBar
				)

				evaluationsNavigation(
					onNavigateToAddEvaluation = {
						navController.navigate(
							EvaluationsDestination.Evaluation(
								evaluationId = null
							)
						)
					},
					onNavigateToEvaluation = { evaluationId ->
						navController.navigate(
							EvaluationsDestination.Evaluation(
								evaluationId = evaluationId
							)
						)
					},
					onNavigateToEvaluationGradePickerDialog = { evaluationId, grade, maxGrade ->
						navController.navigate(
							EvaluationsDestination.EvaluationGradePickerDialog(
								evaluationId = evaluationId,
								grade = grade.toFloat(),
								maxGrade = maxGrade.toFloat()
							)
						)
					},
					onNavigateToEvaluations = {
						navController.navigate(EvaluationsDestination.Evaluations)
					},
					onNavigateToGradePickerDialog = { grade, maxGrade ->
						navController.navigate(
							EvaluationsDestination.GradePickerDialog(
								grade = grade?.toFloat(),
								maxGrade = maxGrade?.toFloat()
							)
						)
					},
					onNavigateToMaxGradePickerDialog = { maxGrade ->
						navController.navigate(
							EvaluationsDestination.MaxGradePickerDialog(
								grade = maxGrade?.toFloat()
							)
						)
					},
					onDismissRequest = {
						navController.navigateUp()
					},
					onSetGrade = { grade ->
						navController
							.viewModel<EvaluationViewModel>()
							?.setGradeAction(
								grade = grade
							)
					},
					onSetMaxGrade = { grade ->
						navController
							.viewModel<EvaluationViewModel>()
							?.setMaxGradeAction(
								grade = grade
							)
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

				aboutNavigation(
					onNavigateToBrowser = { title, url ->
						navController.navigate(
							BrowserDestination.Browser(url = url)
						)
					}
				)

				enrollmentProofFetchNavigation(
					navigateToUpdatePassword = {
						navController.navigate(
							LoginDestination.UpdatePasswordDialog
						)
					},
					onDismissRequest = {
						navController.popBackStack()
					},
					showSnackBar = showSnackBar
				)

				browserNavigation(
					onNavigateToExternalResourceDialog = { url ->
						navController.navigate(
							BrowserDestination.ExternalResourceDialog(
								url = url
							)
						)
					},
					onNavigateToExternalResource = { url ->
						context.browse(url)
					},
					onDismissRequest = {
						navController.navigateUp()
					}
				)
			}
		}
	}
}
