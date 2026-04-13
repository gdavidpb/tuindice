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
		modifier = Modifier.testTag(BaseUiTags.ConfirmationDialogSheet),
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

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 24.dp),
				horizontalArrangement = Arrangement.End
			) {
				OutlinedButton(
					modifier = Modifier.testTag(BaseUiTags.ConfirmationDialogNegativeButton),
					onClick = onDismissRequest,
					border = null,
					enabled = !isLoggingOut
				) {
					Text(text = cancelText)
				}

				if (secondaryText != null) {
					OutlinedButton(
						modifier = Modifier.testTag(AuthUiTags.SignOutSecondaryButton),
						onClick = onSecondaryClick,
						border = null,
						enabled = !isLoggingOut
					) {
						Text(text = secondaryText)
					}
				}

				Button(
					modifier = Modifier.testTag(BaseUiTags.ConfirmationDialogPositiveButton),
					onClick = onConfirmClick,
					enabled = !isLoggingOut
				) {
					Box(
						contentAlignment = Alignment.Center
					) {
						Text(
							text = confirmText,
							color = if (isLoggingOut) {
								Color.Transparent
							} else {
								Color.Unspecified
							}
						)

						if (isLoggingOut) {
							CircularProgressIndicator(
								modifier = Modifier
									.testTag(BaseUiTags.ConfirmationDialogPositiveLoading)
									.size(18.dp),
								color = Color.White
							)
						}
					}
				}
			}
		}
	}
}
