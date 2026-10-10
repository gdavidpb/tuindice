package com.gdavidpb.tuindice.enrollmentproof.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.enrollmentproof.ui.EnrollmentProofUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.enrollmentproof.generated.resources.Res
import tuindice.enrollmentproof.generated.resources.dialog_button_enrollment_cancel
import tuindice.enrollmentproof.generated.resources.dialog_button_enrollment_open_saved
import tuindice.enrollmentproof.generated.resources.dialog_message_enrollment_saved_copy
import tuindice.enrollmentproof.generated.resources.dialog_title_enrollment_saved_copy

/**
 * Asked instead of opening the saved copy straight away: the viewer covers the app, so a notice
 * shown while it opens would never be read.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentProofSavedCopyDialog(
	onOpenSavedClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	ConfirmationDialog(
		sheetState = rememberModalBottomSheetState(),
		titleText = stringResource(Res.string.dialog_title_enrollment_saved_copy),
		positiveText = stringResource(Res.string.dialog_button_enrollment_open_saved),
		negativeText = stringResource(Res.string.dialog_button_enrollment_cancel),
		// The route closes the dialog once the file is handed to the viewer.
		dismissOnPositive = false,
		onPositiveClick = onOpenSavedClick,
		onDismissRequest = onDismissRequest
	) {
		Text(
			modifier = Modifier.testTag(EnrollmentProofUiTags.SavedCopyMessage),
			text = stringResource(Res.string.dialog_message_enrollment_saved_copy),
			style = MaterialTheme.typography.bodyLarge
		)
	}
}
