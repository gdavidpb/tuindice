package com.gdavidpb.tuindice.base.ui.dialog

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
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationDialog(
	sheetState: SheetState,
	titleText: String? = null,
	dismissOnPositive: Boolean = true,
	dismissOnNegative: Boolean = true,
	positiveLoading: Boolean = false,
	positiveEnabled: Boolean = true,
	negativeEnabled: Boolean = true,
	positiveText: String? = null,
	negativeText: String? = null,
	onPositiveClick: () -> Unit = {},
	onNegativeClick: () -> Unit = {},
	onDismissRequest: () -> Unit = {},
	properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
	content: @Composable () -> Unit = {}
) {
	val coroutineScope = rememberCoroutineScope()

	val dismiss = fun() {
		coroutineScope.launch {
			sheetState.hide()
		}.invokeOnCompletion {
			onDismissRequest()
		}
	}

	ModalBottomSheet(
		modifier = Modifier
			.semantics { testTagsAsResourceId = true }
			.testTag(BaseUiTags.ConfirmationDialogSheet),
		sheetState = sheetState,
		onDismissRequest = onDismissRequest,
		properties = properties
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 24.dp)
		) {
			if (titleText != null)
				Text(
					modifier = Modifier
						.testTag(BaseUiTags.ConfirmationDialogTitle)
						.padding(bottom = 16.dp),
					text = titleText,
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Black
				)

			content()

			if (positiveText != null || negativeText != null)
				Row(
					modifier = Modifier
						.fillMaxWidth()
						.padding(vertical = 24.dp),
					horizontalArrangement = Arrangement.End
				) {
					if (negativeText != null)
						OutlinedButton(
							modifier = Modifier.testTag(BaseUiTags.ConfirmationDialogNegativeButton),
							onClick = {
								onNegativeClick()

								if (dismissOnNegative)
									dismiss()
							},
							border = null,
							enabled = negativeEnabled
						) {
							Text(text = negativeText)
						}

					if (positiveText != null)
						Button(
							modifier = Modifier.testTag(BaseUiTags.ConfirmationDialogPositiveButton),
							onClick = {
								onPositiveClick()

								if (dismissOnPositive)
									dismiss()
							},
							enabled = positiveEnabled && !positiveLoading
						) {
							Box(
								contentAlignment = Alignment.Center
							) {
								Text(
									text = positiveText,
									color = if (positiveLoading)
										Color.Transparent
									else
										Color.Unspecified
								)

								if (positiveLoading)
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
