package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags

@Composable
fun ConfirmButton(
	modifier: Modifier = Modifier,
	text: String,
	isLoggingOut: Boolean,
	onClick: () -> Unit
) {
	Button(
		modifier = modifier.testTag(BaseUiTags.ConfirmationDialogPositiveButton),
		onClick = onClick,
		enabled = !isLoggingOut
	) {
		Box(
			contentAlignment = Alignment.Center
		) {
			Text(
				text = text,
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
					color = LocalContentColor.current
				)
			}
		}
	}
}
