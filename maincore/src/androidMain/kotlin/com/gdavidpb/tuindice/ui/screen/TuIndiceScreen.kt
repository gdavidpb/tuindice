package com.gdavidpb.tuindice.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.gdavidpb.tuindice.base.presentation.model.SnackBarMessage
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.navigation.Destination
import com.gdavidpb.tuindice.base.ui.dialog.ExternalResourceDialog
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.ErrorView
import com.gdavidpb.tuindice.base.ui.view.TopAppBarActionsView
import com.gdavidpb.tuindice.base.ui.view.TopAppBarAnimatedTitleView
import com.gdavidpb.tuindice.base.utils.extension.isCurrentDestination
import com.gdavidpb.tuindice.base.utils.extension.viewModelFlow
import com.gdavidpb.tuindice.evaluations.ui.screen.EvaluationGradePickerContentDialog
import com.gdavidpb.tuindice.evaluations.ui.screen.GradePickerContentDialog
import com.gdavidpb.tuindice.evaluations.ui.screen.MaxGradePickerContentDialog
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.model.BottomBarConfig
import com.gdavidpb.tuindice.summary.presentation.route.ProfilePictureActionsFactory
import com.gdavidpb.tuindice.summary.ui.screen.ProfilePictureSettingsContentDialog
import com.gdavidpb.tuindice.summary.ui.screen.RemoveProfilePictureConfirmationContentDialog
import com.gdavidpb.tuindice.ui.dialog.GooglePlayServicesDialog
import com.gdavidpb.tuindice.ui.resource.HostUiTextProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuIndiceScreen(
	state: Main.State,
	updateState: (Main.State) -> Unit,
	onRetryStartUp: () -> Unit,
	navController: NavHostController,
	snackbarHostState: SnackbarHostState,
	onAction: (action: TopBarAction) -> Unit,
	onNavigateTo: (destination: Destination) -> Unit,
	onNavigateBack: () -> Unit,
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
	showSnackBar: (message: SnackBarMessage) -> Unit
) {
	when (state) {
		is Main.State.Starting -> {
			Box(modifier = Modifier.fillMaxSize()) {
				CircularProgressIndicator(
					modifier = Modifier.align(Alignment.Center)
				)
			}
			return
		}

		is Main.State.Failed -> {
			ErrorView(
				title = "Inicio",
				message = "No se pudo iniciar la app.",
				retryText = "Reintentar",
				onRetryClick = onRetryStartUp,
				headerContent = { ErrorStateAnimationView() }
			)
			return
		}

		is Main.State.Content -> Unit
	}

	val contentState = state
	val profilePictureActionsProvider = koinInject<ProfilePictureActionsFactory>()
	val hostUiTexts = koinInject<HostUiTextProvider>().getValues()

	LaunchedEffect(navController) {
		navController
			.viewModelFlow()
			.flowOn(Dispatchers.IO)
			.collectLatest { viewModel ->
				viewModel.state.collect { currentViewState ->
					updateState(
						contentState.copy(
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
						title = contentState.topBarTitle
					)
				},
				actions = {
					TopAppBarActionsView(
						topBarConfig = contentState.topBarConfig,
						onAction = onAction,
						actionIconContent = { action ->
							Icon(
								imageVector = action.getIcon(),
								contentDescription = null
							)
						}
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
			if (contentState.isBottomBarVisible) {
				NavigationBar(
					modifier = Modifier.height(64.dp),
					containerColor = MaterialTheme.colorScheme.onSecondary
				) {
					bottomBarConfigs.forEach { bottomBarConfig ->
						val isNavigationBarItemSelected = navController
							.isCurrentDestination(destination = bottomBarConfig.destination)

						val navigationBarItemIcon =
							bottomBarIcon(
								config = bottomBarConfig,
								selected = isNavigationBarItemSelected
							)

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
		TuIndiceNavHost(
			navController = navController,
			startDestination = contentState.startDestination,
			modifier = Modifier.padding(innerPadding),
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
			showSnackBar = showSnackBar,
			googlePlayServicesDialogContent = { confirm, dismiss ->
				GooglePlayServicesDialog(
					titleText = hostUiTexts.googleServicesUnavailableTitle,
					messageText = hostUiTexts.googleServicesUnavailableMessage,
					exitText = hostUiTexts.googleServicesUnavailableExit,
					onConfirmExitClick = confirm,
					onDismissRequest = dismiss
				)
			},
			profilePictureActionsFactory = { viewModel ->
				profilePictureActionsProvider.remember(
					onPicturePicked = viewModel::uploadProfilePictureAction,
					onPictureTaken = viewModel::uploadTakenProfilePictureAction
				)
			},
			removeProfilePictureConfirmationDialogContent = { onConfirmClick, onDismissRequest ->
				RemoveProfilePictureConfirmationContentDialog(
					onConfirmClick = onConfirmClick,
					onDismissRequest = onDismissRequest
				)
			},
			profilePictureSettingsDialogContent = { showRemove, cameraAvailable, onPickPictureClick, onTakePictureClick, onRemovePictureClick, onDismissRequest ->
				ProfilePictureSettingsContentDialog(
					showRemove = showRemove,
					isCameraAvailable = cameraAvailable,
					onPickPictureClick = onPickPictureClick,
					onTakePictureClick = onTakePictureClick,
					onRemovePictureClick = onRemovePictureClick,
					onDismissRequest = onDismissRequest
				)
			},
			gradePickerDialogContent = { selectedGrade, maxGrade, onGradeChange, onDismissRequest ->
				GradePickerContentDialog(
					selectedGrade = selectedGrade,
					maxGrade = maxGrade,
					onGradeChange = onGradeChange,
					onDismissRequest = onDismissRequest
				)
			},
			maxGradePickerDialogContent = { selectedGrade, onGradeChange, onDismissRequest ->
				MaxGradePickerContentDialog(
					selectedGrade = selectedGrade,
					onGradeChange = onGradeChange,
					onDismissRequest = onDismissRequest
				)
			},
			evaluationGradePickerDialogContent = { selectedGrade, maxGrade, onGradeChange, onDismissRequest ->
				EvaluationGradePickerContentDialog(
					selectedGrade = selectedGrade,
					maxGrade = maxGrade,
					onGradeChange = onGradeChange,
					onDismissRequest = onDismissRequest
				)
			},
			externalResourceDialogContent = { url, onConfirmClick, onDismissRequest ->
				ExternalResourceDialog(
					url = url,
					titleText = hostUiTexts.externalResourceTitle,
					messageText = hostUiTexts.externalResourceMessage,
					openText = hostUiTexts.externalResourceOpen,
					cancelText = hostUiTexts.externalResourceCancel,
					onConfirmClick = onConfirmClick,
					onDismissRequest = onDismissRequest
				)
			}
		)
	}
}

private fun TopBarAction.getIcon(): ImageVector {
	return when (this) {
		is TopBarAction.SignOutAction ->
			Icons.AutoMirrored.Outlined.Logout
		is TopBarAction.FetchEnrollmentProofAction ->
			Icons.Outlined.FindInPage
	}
}

private fun bottomBarIcon(
	config: BottomBarConfig,
	selected: Boolean
): ImageVector = when (config) {
	BottomBarConfig.Summary ->
		if (selected) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder
	BottomBarConfig.Record ->
		if (selected) Icons.Filled.Book else Icons.Outlined.Book
	BottomBarConfig.Evaluations ->
		if (selected) Icons.Filled.DateRange else Icons.Outlined.DateRange
	BottomBarConfig.About ->
		if (selected) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder
}
