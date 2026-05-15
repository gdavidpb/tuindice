package com.gdavidpb.tuindice.auth.ui.dialog

import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.auth.ui.AuthUiTags

@Composable
fun SecondaryButton(
	text: String,
	enabled: Boolean,
	onClick: () -> Unit
) {
	OutlinedButton(
		modifier = Modifier.testTag(AuthUiTags.SignOutSecondaryButton),
		onClick = onClick,
		border = null,
		enabled = enabled
	) {
		Text(text = text)
	}
}
