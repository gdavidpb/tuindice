package com.gdavidpb.tuindice.ui.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.gdavidpb.tuindice.base.utils.extension.isCurrentDestination
import com.gdavidpb.tuindice.base.utils.extension.viewModel
import com.gdavidpb.tuindice.base.utils.extension.viewModelFlow
import com.gdavidpb.tuindice.enrollmentproof.presentation.navigation.enrollmentProofFetchNavigation
import com.gdavidpb.tuindice.evaluations.presentation.navigation.EvaluationsDestination
import com.gdavidpb.tuindice.evaluations.presentation.navigation.evaluationsNavigation
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationViewModel
import com.gdavidpb.tuindice.evaluations.presentation.viewmodel.EvaluationsViewModel
import com.gdavidpb.tuindice.login.presentation.navigation.LoginDestination
import com.gdavidpb.tuindice.login.presentation.navigation.loginNavigation
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.model.BottomBarConfig
import com.gdavidpb.tuindice.presentation.navigation.BrowserDestination
import com.gdavidpb.tuindice.presentation.navigation.browserNavigation
import com.gdavidpb.tuindice.presentation.navigation.mainNavigation
import com.gdavidpb.tuindice.record.presentation.navigation.recordNavigation
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.summaryNavigation
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuIndiceScreen(
	state: Main.State,
	updateState: (Main.State) -> Unit,
	navController: NavHostController,
	snackbarHostState: SnackbarHostState,
	onAction: (action: TopBarAction) -> Unit,
	onNavigateTo: (destination: Destination) -> Unit,
	onNavigateBack: () -> Unit,
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	if (state !is Main.State.Content) return

	LaunchedEffect(navController) {
		navController
			.viewModelFlow()
			.flowOn(Dispatchers.IO)
			.collectLatest { viewModel ->
				viewModel.state.collect { currentViewState ->
					updateState(
						state.copy(
							topBarTitle = currentViewState.topBarTitle,
							topBarConfig = currentViewState.topBarConfig,
							isTopBarVisible = currentViewState.isTopBarVisible,
							isBottomBarVisible = currentViewState.isBottomBarVisible
						)
					)
				}
			}
	}

	val bottomBarConfigs = remember {
		listOf(
			BottomBarConfig.Summary,
			BottomBarConfig.Record,
			BottomBarConfig.Evaluations,
			BottomBarConfig.About
		)
	}

	Scaffold(
		snackbarHost = { SnackbarHost(snackbarHostState) },
		topBar = {
			TopAppBar(
				title = {
					TopAppBarAnimatedTitleView(
						title = state.topBarTitle
					)
				},
				actions = {
					TopAppBarActionsView(
						topBarConfig = state.topBarConfig,
						onAction = onAction
					)
				},
				navigationIcon = {
					val hasPreviousBackStackEntry = (navController.previousBackStackEntry != null)

					if (hasPreviousBackStackEntry)
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
			if (state.isBottomBarVisible) {
				NavigationBar(
					modifier = Modifier.height(dimensionResource(id = R.dimen.dp_48)),
					containerColor = MaterialTheme.colorScheme.onSecondary
				) {
					bottomBarConfigs.forEach { bottomBarConfig ->
						val isNavigationBarItemSelected = navController
							.isCurrentDestination(destination = bottomBarConfig.destination)

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
							onClick = { onNavigateTo(bottomBarConfig.destination) }
						)
					}
				}
			}
		}
	) { innerPadding ->
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
					navController.navigate(
						BrowserDestination.Browser(
							title = title,
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
							grade = grade,
							maxGrade = maxGrade
						)
					)
				},
				onNavigateToEvaluations = {
					navController.navigate(EvaluationsDestination.Evaluations)
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
					navController.navigate(
						EvaluationsDestination.MaxGradePickerDialog(
							grade = maxGrade
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
						BrowserDestination.Browser(
							title = title,
							url = url
						)
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
