package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SignOutDialogActions(
	modifier: Modifier = Modifier,
	confirmText: String,
	secondaryText: String?,
	cancelText: String,
	isLoggingOut: Boolean,
	onConfirmClick: () -> Unit,
	onSecondaryClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	if (secondaryText == null) {
		Row(
			modifier = modifier,
			horizontalArrangement = Arrangement.End
		) {
			CancelButton(
				text = cancelText,
				enabled = !isLoggingOut,
				onClick = onDismissRequest
			)

			ConfirmButton(
				text = confirmText,
				isLoggingOut = isLoggingOut,
				onClick = onConfirmClick
			)
		}
	} else {
		Column(
			modifier = modifier,
			horizontalAlignment = Alignment.End
		) {
			ConfirmButton(
				modifier = Modifier.fillMaxWidth(),
				text = confirmText,
				isLoggingOut = isLoggingOut,
				onClick = onConfirmClick
			)

			Row(
				modifier = Modifier.padding(top = 8.dp),
				horizontalArrangement = Arrangement.End
			) {
				CancelButton(
					text = cancelText,
					enabled = !isLoggingOut,
					onClick = onDismissRequest
				)

				SecondaryButton(
					text = secondaryText,
					enabled = !isLoggingOut,
					onClick = onSecondaryClick
				)
			}
		}
	}
}
