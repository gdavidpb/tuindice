package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.ui.AuthUiTags
import com.gdavidpb.tuindice.base.ui.BaseUiTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignOutDialog(
	state: SignOut.State,
	titleText: String,
	messageText: String,
	confirmText: String,
	secondaryText: String?,
	cancelText: String,
	onConfirmClick: () -> Unit,
	onSecondaryClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()
	val isLoggingOut = state is SignOut.State.LoggingOut

	ModalBottomSheet(
		modifier = Modifier
			.semantics { testTagsAsResourceId = true }
			.testTag(BaseUiTags.ConfirmationDialogSheet),
		sheetState = sheetState,
		onDismissRequest = {
			if (!isLoggingOut) {
				onDismissRequest()
			}
		}
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 24.dp)
		) {
			Text(
				modifier = Modifier
					.testTag(BaseUiTags.ConfirmationDialogTitle)
					.padding(bottom = 16.dp),
				text = titleText,
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Black
			)

			Text(
				modifier = Modifier.testTag(AuthUiTags.SignOutMessageText),
				text = messageText,
				style = MaterialTheme.typography.bodyLarge
			)

			SignOutDialogActions(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 24.dp),
				confirmText = confirmText,
				secondaryText = secondaryText,
				cancelText = cancelText,
				isLoggingOut = isLoggingOut,
				onConfirmClick = onConfirmClick,
				onSecondaryClick = onSecondaryClick,
				onDismissRequest = onDismissRequest
			)
		}
	}
}
