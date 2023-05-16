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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
	titleText: String,
	positiveText: String,
	negativeText: String,
	onPositiveClick: () -> Unit,
	onNegativeClick: () -> Unit,
	onDismissRequest: () -> Unit,
	content: @Composable () -> Unit
) {
	val coroutineScope = rememberCoroutineScope()
	val modalBottomSheetState = rememberModalBottomSheetState()

	val animateToDismiss: (() -> Unit) -> Unit = { onCompletion ->
		coroutineScope
			.launch { modalBottomSheetState.hide() }
			.invokeOnCompletion {
				onCompletion()
				onDismissRequest()
			}
	}

	ModalBottomSheet(
		sheetState = modalBottomSheetState,
		onDismissRequest = onDismissRequest
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = dimensionResource(id = R.dimen.dp_16))
		) {
			Text(
				text = titleText,
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.Medium
			)
			content()

			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = dimensionResource(id = R.dimen.dp_24)),
				horizontalArrangement = Arrangement.End
			) {
				OutlinedButton(
					onClick = {
						animateToDismiss(onNegativeClick)
					},
					border = null
				) {
					Text(text = negativeText)
				}

				Button(
					onClick = {
						animateToDismiss(onPositiveClick)
					},
				) {
					Text(text = positiveText)
				}
			}
		}
	}
}