package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.dimensionResource
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.summary.R
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.ui.dialog.ProfilePictureSettingsDialog
import com.gdavidpb.tuindice.summary.ui.dialog.RemoveProfilePictureConfirmationDialog
import com.gdavidpb.tuindice.summary.ui.view.ProfilePictureView
import com.gdavidpb.tuindice.summary.ui.view.SummaryContentView
import com.gdavidpb.tuindice.summary.ui.view.SummaryFailedView
import com.gdavidpb.tuindice.summary.ui.view.SummaryLoadingView
import com.gdavidpb.tuindice.summary.ui.view.rememberSummaryItems
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.*

@Composable
fun SummaryContentScreen(
	state: Summary.State,
	onRetryClick: () -> Unit,
	onEditProfilePictureClick: () -> Unit
) {
	SummaryScreen(
		state = state,
		onRetryClick = onRetryClick,
		onEditProfilePictureClick = onEditProfilePictureClick,
		loadingContent = {
			SummaryLoadingView()
		},
		failedContent = { retry ->
			SummaryFailedView(
				title = stringResource(Res.string.summary_failed_title),
				message = stringResource(Res.string.summary_failed_message),
				retryText = stringResource(Res.string.summary_failed_retry),
				onRetryClick = retry,
				headerContent = {
					ErrorStateAnimationView()
				}
			)
		},
		contentStateContent = { contentState, editClick ->
			SummaryContentView(
				state = contentState,
				summaryItems = rememberSummaryItems(state = contentState),
				onEditProfilePictureClick = editClick,
				profilePictureContent = { profilePictureState, onLoading, onClick ->
					ProfilePictureView(
						state = profilePictureState,
						onLoading = onLoading,
						onClick = onClick
					)
				},
				lastUpdateLeadingContent = { _, isUpdated, rotation ->
					Icon(
						modifier = Modifier
							.padding(horizontal = dimensionResource(id = R.dimen.dp_4))
							.rotate(rotation),
						imageVector = if (isUpdated) Icons.Outlined.Sync else Icons.Outlined.SyncProblem,
						tint = if (isUpdated) LocalContentColor.current else MaterialTheme.colorScheme.error,
						contentDescription = null
					)
				}
			)
		}
	)
}

@Composable
fun RemoveProfilePictureConfirmationContentDialog(
	onConfirmClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	RemoveProfilePictureConfirmationDialog(
		titleText = stringResource(Res.string.dialog_title_remove_profile_picture),
		messageText = stringResource(Res.string.dialog_message_remove_profile_picture),
		confirmText = stringResource(Res.string.dialog_button_remove),
		cancelText = stringResource(Res.string.dialog_button_cancel),
		onConfirmClick = onConfirmClick,
		onDismissRequest = onDismissRequest
	)
}

@Composable
fun ProfilePictureSettingsContentDialog(
	showRemove: Boolean,
	isCameraAvailable: Boolean,
	onPickPictureClick: () -> Unit,
	onTakePictureClick: () -> Unit,
	onRemovePictureClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	ProfilePictureSettingsDialog(
		showRemove = showRemove,
		isCameraAvailable = isCameraAvailable,
		titleText = stringResource(Res.string.title_menu_profile_picture),
		pickPictureLabel = stringResource(Res.string.menu_pick_profile_picture),
		takePictureLabel = stringResource(Res.string.menu_take_profile_picture),
		removePictureLabel = stringResource(Res.string.menu_remove_profile_picture),
		onPickPictureClick = onPickPictureClick,
		onTakePictureClick = onTakePictureClick,
		onRemovePictureClick = onRemovePictureClick,
		onDismissRequest = onDismissRequest
	)
}
