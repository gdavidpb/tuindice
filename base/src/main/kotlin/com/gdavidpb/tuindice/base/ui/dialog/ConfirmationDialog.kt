package com.gdavidpb.tuindice.base.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationDialog(
	sheetState: SheetState,
	titleText: String,
	positiveText: String? = null,
	negativeText: String? = null,
	onPositiveClick: () -> Unit = {},
	onNegativeClick: () -> Unit = {},
	onDismissRequest: () -> Unit = {},
	content: @Composable () -> Unit = {}
) {
	val coroutineScope = rememberCoroutineScope()

	ModalBottomSheet(
		sheetState = sheetState,
		onDismissRequest = onDismissRequest
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = dimensionResource(id = R.dimen.dp_16))
		) {
			Text(
				modifier = Modifier.padding(bottom = dimensionResource(id = R.dimen.dp_16)),
				text = titleText,
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Medium
			)

			content()

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = dimensionResource(id = R.dimen.dp_24)),
				horizontalArrangement = Arrangement.End
			) {
				if (negativeText != null)
					OutlinedButton(
						onClick = {
							coroutineScope.launch {
								sheetState.hide()
							}.invokeOnCompletion {
								onDismissRequest()
								onNegativeClick()
							}
						},
						border = null
					) {
						Text(text = negativeText)
					}

				if (positiveText != null)
					Button(
						onClick = {
							coroutineScope.launch {
								sheetState.hide()
							}.invokeOnCompletion {
								onDismissRequest()
								onPositiveClick()
							}
						},
					) {
						Text(text = positiveText)
					}
			}
		}
	}
}