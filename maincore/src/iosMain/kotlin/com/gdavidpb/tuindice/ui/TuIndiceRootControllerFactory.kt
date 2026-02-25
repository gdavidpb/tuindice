package com.gdavidpb.tuindice.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.gdavidpb.tuindice.about.ui.screen.AboutScreen
import com.gdavidpb.tuindice.base.domain.repository.BrowserRepository
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.ReviewRepository
import com.gdavidpb.tuindice.base.domain.repository.UpdateRepository
import com.gdavidpb.tuindice.base.ui.dialog.ExternalResourceDialog
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.ErrorView
import com.gdavidpb.tuindice.di.IosPlatformBridge
import com.gdavidpb.tuindice.di.requireIosKoin
import com.gdavidpb.tuindice.evaluations.ui.screen.*
import com.gdavidpb.tuindice.enrollmentproof.ui.screen.EnrollmentProofScreen
import com.gdavidpb.tuindice.login.ui.screen.SignInScreen
import com.gdavidpb.tuindice.login.ui.screen.SignOutScreen
import com.gdavidpb.tuindice.login.ui.screen.UpdatePasswordScreen
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.route.TuIndiceCoordinatorRoute
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.record.ui.screen.RecordScreen
import com.gdavidpb.tuindice.summary.presentation.route.ProfilePictureActionsFactory
import com.gdavidpb.tuindice.summary.ui.screen.ProfilePictureSettingsContentDialog
import com.gdavidpb.tuindice.summary.ui.screen.RemoveProfilePictureConfirmationContentDialog
import com.gdavidpb.tuindice.summary.ui.screen.SummaryScreen
import com.gdavidpb.tuindice.ui.dialog.GooglePlayServicesDialog
import com.gdavidpb.tuindice.ui.resource.HostUiTextProvider
import com.gdavidpb.tuindice.ui.screen.BrowserScreen
import com.gdavidpb.tuindice.ui.screen.TuIndiceNavHost
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
	val koin = remember { requireIosKoin() }
	val uiText = remember(koin) { koin.get<HostUiTextProvider>() }.getValues()
	val profilePictureActionsProvider = remember(koin) { koin.get<ProfilePictureActionsFactory>() }
	val iosBridge = remember(koin) { koin.get<IosPlatformBridge>() }
	val browserGateway = remember(iosBridge) {
		object : BrowserRepository {
			override fun open(url: String) {
				iosBridge.openUrl(url)
			}
		}
	}
	val deviceInfoGateway = remember(iosBridge) {
		object : DeviceInfoRepository {
			override fun appVersionName(): String = iosBridge.appVersionName()

			override fun appVersionCode(): Long = iosBridge.appVersionCode()

			override fun hasCamera(): Boolean = iosBridge.hasCamera()
		}
	}
	val reviewGateway = remember(koin) { koin.get<ReviewRepository>() }
	val updateGateway = remember(koin) { koin.get<UpdateRepository>() }
	val mainViewModel = remember(koin) { koin.get<MainViewModel>() }
	val coroutineScope = rememberCoroutineScope()
	val snackBarHostState = remember { SnackbarHostState() }

	TuIndiceSharedTheme {
		Box(modifier = Modifier.fillMaxSize()) {
			TuIndiceCoordinatorRoute(
				onConfirmExitClick = {},
				showSnackBar = { message ->
					coroutineScope.launch {
						snackBarHostState.currentSnackbarData?.dismiss()

						val snackBarResult = snackBarHostState.showSnackbar(
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
				},
				browserGateway = browserGateway,
				deviceInfoGateway = deviceInfoGateway,
				reviewGateway = reviewGateway,
				updateGateway = updateGateway,
				viewModel = mainViewModel
			) { state, _, onRetryStartUp, navController, _, _, _, isCameraAvailable, onNavigateToExternalResource, onConfirmRemoveProfilePicture, onPickProfilePicture, onTakeProfilePicture, onRemoveProfilePicture, onSetGrade, onSetMaxGrade, onSetEvaluationGrade, showSnackBar ->
				when (state) {
					is Main.State.Starting -> {
						Box(modifier = Modifier.fillMaxSize()) {
							CircularProgressIndicator(
								modifier = Modifier.align(Alignment.Center)
							)
						}
						return@TuIndiceCoordinatorRoute
					}

					is Main.State.Failed -> {
						ErrorView(
							title = "Inicio",
							message = "No se pudo iniciar la app.",
							retryText = "Reintentar",
							onRetryClick = onRetryStartUp,
							headerContent = { ErrorStateAnimationView() }
						)
						return@TuIndiceCoordinatorRoute
					}

					is Main.State.Content -> Unit
				}

				val contentState = state

				TuIndiceNavHost(
					navController = navController,
					startDestination = contentState.startDestination,
					isSwipeBackNavigationEnabled = true,
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
					showSnackBar = showSnackBar,
					googlePlayServicesDialogContent = { confirm, dismiss ->
						GooglePlayServicesDialog(
							titleText = uiText.googleServicesUnavailableTitle,
							messageText = uiText.googleServicesUnavailableMessage,
							exitText = uiText.googleServicesUnavailableExit,
							onConfirmExitClick = confirm,
							onDismissRequest = dismiss
						)
					},
					signInContent = { signInState, usbIdChange, passwordChange, signInClick, termsClick, privacyClick ->
						SignInScreen(
							state = signInState,
							onUsbIdChange = usbIdChange,
							onPasswordChange = passwordChange,
							onSignInClick = signInClick,
							onTermsAndConditionsClick = termsClick,
							onPrivacyPolicyClick = privacyClick
						)
					},
					signOutDialogContent = { signOutState, confirmClick, dismissRequest ->
						SignOutScreen(
							state = signOutState,
							onConfirmClick = confirmClick,
							onDismissRequest = dismissRequest
						)
					},
					updatePasswordDialogContent = { updatePasswordState, passwordChange, confirmClick, dismissRequest ->
						UpdatePasswordScreen(
							state = updatePasswordState,
							onPasswordChange = passwordChange,
							onConfirmClick = confirmClick,
							onDismissRequest = dismissRequest
						)
					},
					profilePictureActionsFactory = { viewModel ->
						profilePictureActionsProvider.remember(
							onPicturePicked = viewModel::uploadProfilePictureAction,
							onPictureTaken = viewModel::uploadTakenProfilePictureAction
						)
					},
					summaryContent = { summaryState, retryClick, editProfilePictureClick ->
						SummaryScreen(
							state = summaryState,
							onRetryClick = retryClick,
							onEditProfilePictureClick = editProfilePictureClick
						)
					},
					removeProfilePictureConfirmationDialogContent = { confirmClick, dismissRequest ->
						RemoveProfilePictureConfirmationContentDialog(
							onConfirmClick = confirmClick,
							onDismissRequest = dismissRequest
						)
					},
					profilePictureSettingsDialogContent = { showRemove, cameraAvailable, pickPictureClick, takePictureClick, removePictureClick, dismissRequest ->
						ProfilePictureSettingsContentDialog(
							showRemove = showRemove,
							isCameraAvailable = cameraAvailable,
							onPickPictureClick = pickPictureClick,
							onTakePictureClick = takePictureClick,
							onRemovePictureClick = removePictureClick,
							onDismissRequest = dismissRequest
						)
					},
					recordContent = { recordState, retryClick, subjectGradeChange ->
						RecordScreen(
							state = recordState,
							onRetryClick = retryClick,
							onSubjectGradeChange = subjectGradeChange
						)
					},
					evaluationsContent = { evaluationsState, addEvaluationClick, evaluationClick, evaluationEdit, evaluationDelete, filterCheckedChange, clearFiltersClick, retryClick ->
						EvaluationsScreen(
							state = evaluationsState,
							onAddEvaluationClick = addEvaluationClick,
							onEvaluationClick = evaluationClick,
							onEvaluationEdit = evaluationEdit,
							onEvaluationDelete = evaluationDelete,
							onFilterCheckedChange = filterCheckedChange,
							onClearFiltersClick = clearFiltersClick,
							onRetryClick = retryClick
						)
					},
					evaluationContent = { evaluationState, subjectChange, typeChange, dateChange, gradeClick, maxGradeClick, doneClick, retryClick ->
						EvaluationScreen(
							state = evaluationState,
							onSubjectChange = subjectChange,
							onTypeChange = typeChange,
							onDateChange = dateChange,
							onGradeClick = gradeClick,
							onMaxGradeClick = maxGradeClick,
							onDoneClick = doneClick,
							onRetryClick = retryClick
						)
					},
					gradePickerDialogContent = { selectedGrade, maxGrade, gradeChange, dismissRequest ->
						GradePickerContentDialog(
							selectedGrade = selectedGrade,
							maxGrade = maxGrade,
							onGradeChange = gradeChange,
							onDismissRequest = dismissRequest
						)
					},
					maxGradePickerDialogContent = { selectedGrade, gradeChange, dismissRequest ->
						MaxGradePickerContentDialog(
							selectedGrade = selectedGrade,
							onGradeChange = gradeChange,
							onDismissRequest = dismissRequest
						)
					},
					evaluationGradePickerDialogContent = { selectedGrade, maxGrade, gradeChange, dismissRequest ->
						EvaluationGradePickerContentDialog(
							selectedGrade = selectedGrade,
							maxGrade = maxGrade,
							onGradeChange = gradeChange,
							onDismissRequest = dismissRequest
						)
					},
					aboutContent = { aboutState, creativeCommonsClick, xClick, githubClick, kotlinClick, composeClick, firebaseClick, koinClick, ktorClick, dstClick, termsClick, privacyClick, shareClick, rateClick, contactClick, reportClick ->
						AboutScreen(
							state = aboutState,
							onCreativeCommonsClick = creativeCommonsClick,
							onXClick = xClick,
							onGithubClick = githubClick,
							onKotlinClick = kotlinClick,
							onComposeClick = composeClick,
							onFirebaseClick = firebaseClick,
							onKoinClick = koinClick,
							onKtorClick = ktorClick,
							onDstClick = dstClick,
							onTermsAndConditionsClick = termsClick,
							onPrivacyPolicyClick = privacyClick,
							onShareAppClick = shareClick,
							onRateOnPlayStoreClick = rateClick,
							onContactDeveloperClick = contactClick,
							onReportBugClick = reportClick
						)
					},
					enrollmentProofDialogContent = { enrollmentState, dismissRequest ->
						EnrollmentProofScreen(
							state = enrollmentState,
							onDismissRequest = dismissRequest
						)
					},
					browserContent = { browserState, pageStarted, pageFinished, externalResourceClick ->
						BrowserScreen(
							state = browserState,
							onPageStarted = pageStarted,
							onPageFinished = pageFinished,
							onExternalResourceClick = externalResourceClick
						)
					},
					externalResourceDialogContent = { url, confirmClick, dismissRequest ->
						ExternalResourceDialog(
							url = url,
							titleText = uiText.externalResourceTitle,
							messageText = uiText.externalResourceMessage,
							openText = uiText.externalResourceOpen,
							cancelText = uiText.externalResourceCancel,
							onConfirmClick = confirmClick,
							onDismissRequest = dismissRequest
						)
					}
				)
			}

			SnackbarHost(
				hostState = snackBarHostState,
				modifier = Modifier.align(Alignment.BottomCenter)
			)
		}
	}
}
