package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.ui.view.getAnnotatedUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExternalResourceDialog(
	url: String,
	titleText: String,
	messageText: String,
	openText: String,
	cancelText: String,
	onConfirmClick: (url: String) -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()

	ConfirmationDialog(
		sheetState = sheetState,
		titleText = titleText,
		positiveText = openText,
		negativeText = cancelText,
		onPositiveClick = { onConfirmClick(url) },
		onDismissRequest = onDismissRequest
	) {
		Text(
			modifier = Modifier.testTag(BaseUiTags.ExternalResourceMessage),
			text = messageText,
			style = MaterialTheme.typography.bodyLarge
		)
		Text(
			modifier = Modifier.testTag(BaseUiTags.ExternalResourceUrl),
			text = getAnnotatedUrl(
				url = url,
				primary = MaterialTheme.colorScheme.primary,
				outlineVariant = MaterialTheme.colorScheme.outlineVariant
			),
			style = MaterialTheme.typography.bodyMedium
		)
	}
}
