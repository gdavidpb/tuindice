package com.gdavidpb.tuindice.summary.ui.dialog

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfilePictureSettingsDialog(
	showRemove: Boolean,
	isCameraAvailable: Boolean,
	titleText: String,
	pickPictureLabel: String,
	takePictureLabel: String,
	removePictureLabel: String,
	onPickPictureClick: () -> Unit,
	onTakePictureClick: () -> Unit,
	onRemovePictureClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()
	val coroutineScope = rememberCoroutineScope()

	val dismissAndRun = fun(action: () -> Unit) {
		coroutineScope.launch {
			sheetState.hide()
		}.invokeOnCompletion {
			onDismissRequest()
			action()
		}
	}

	ConfirmationDialog(
		sheetState = sheetState,
		titleText = titleText,
		onDismissRequest = onDismissRequest
	) {
		TextButton(onClick = { dismissAndRun(onPickPictureClick) }) {
			Text(pickPictureLabel)
		}

		if (isCameraAvailable)
			TextButton(onClick = { dismissAndRun(onTakePictureClick) }) {
				Text(takePictureLabel)
			}

		if (showRemove)
			TextButton(onClick = { dismissAndRun(onRemovePictureClick) }) {
				Text(
					text = removePictureLabel,
					color = MaterialTheme.colorScheme.error
				)
			}

		Spacer(
			modifier = Modifier
				.size(16.dp)
		)
	}
}
