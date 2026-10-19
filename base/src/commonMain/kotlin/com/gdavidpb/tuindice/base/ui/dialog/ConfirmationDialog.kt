package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.ui.exposeTestTagsAsResourceId
import com.gdavidpb.tuindice.base.ui.keyboard.bringRegionIntoViewOnFocus
import com.gdavidpb.tuindice.base.ui.keyboard.dismissKeyboardOnScroll
import com.gdavidpb.tuindice.base.ui.keyboard.dismissKeyboardOnTap
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import kotlinx.coroutines.launch

// A sheet that holds a text field sets [scrollable]: the whole body (title, content and buttons) then scrolls, and the
// field and the positive action stay above the keyboard, so the sheet shrinks instead of being squeezed. The other
// sheets keep their own scrolling (the schedule grid), so they are not nested in a second scroll.
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
	positiveButtonTestTag: String = BaseUiTags.ConfirmationDialogPositiveButton,
	positiveLoadingTestTag: String = BaseUiTags.ConfirmationDialogPositiveLoading,
	positiveText: String? = null,
	negativeText: String? = null,
	onPositiveClick: () -> Unit = {},
	onNegativeClick: () -> Unit = {},
	onDismissRequest: () -> Unit = {},
	properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
	scrollable: Boolean = false,
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
			.exposeTestTagsAsResourceId()
			.testTag(BaseUiTags.ConfirmationDialogSheet),
		sheetState = sheetState,
		onDismissRequest = onDismissRequest,
		properties = properties
	) {
		val focusManager = LocalFocusManager.current
		val fieldRegion = remember { BringIntoViewRequester() }

		val body: @Composable () -> Unit = {
			if (titleText != null)
				Text(
					modifier = Modifier
						.testTag(BaseUiTags.ConfirmationDialogTitle)
						.padding(bottom = 16.dp),
					text = titleText,
					style = MaterialTheme.typography.titleLarge,
					fontWeight = FontWeight.Bold
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
							modifier = Modifier.testTag(positiveButtonTestTag),
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
											.testTag(positiveLoadingTestTag)
											.size(18.dp),
										color = LocalContentColor.current
									)
							}
						}
				}
		}

		if (scrollable) {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.imePadding()
					.dismissKeyboardOnTap(focusManager)
					.dismissKeyboardOnScroll(focusManager)
					.verticalScroll(rememberScrollState())
					.padding(horizontal = TuIndiceSpacing.Dialog)
			) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.bringRegionIntoViewOnFocus(fieldRegion)
				) {
					body()
				}
			}
		} else {
			Column(
				modifier = Modifier
					.fillMaxWidth()
					.imePadding()
					.padding(horizontal = TuIndiceSpacing.Dialog)
			) {
				body()
			}
		}
	}
}
