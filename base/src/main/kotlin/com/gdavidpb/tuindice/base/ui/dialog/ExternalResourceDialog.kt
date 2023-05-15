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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.R
import com.gdavidpb.tuindice.base.ui.view.getAnnotatedUrl
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExternalResourceDialog(
	url: String,
	onConfirmClick: () -> Unit,
	onCancelClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val coroutineScope = rememberCoroutineScope()
	val modalBottomSheetState = rememberModalBottomSheetState()

	val animateToDismiss = {
		coroutineScope
			.launch { modalBottomSheetState.hide() }
			.invokeOnCompletion {
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
				text = stringResource(id = R.string.dialog_title_warning_external),
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.Medium
			)
			Text(
				modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.dp_16)),
				text = stringResource(id = R.string.dialog_message_warning_external),
				style = MaterialTheme.typography.bodyMedium
			)
			Text(
				text = getAnnotatedUrl(url = url),
				style = MaterialTheme.typography.bodyMedium
			)
			Row(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = dimensionResource(id = R.dimen.dp_24)),
				horizontalArrangement = Arrangement.End
			) {
				OutlinedButton(
					onClick = {
						onCancelClick()
						animateToDismiss()
					},
					border = null
				) {
					Text(text = stringResource(id = R.string.cancel))
				}

				Button(
					onClick = {
						onConfirmClick()
						animateToDismiss()
					},
				) {
					Text(text = stringResource(id = R.string.open))
				}
			}
		}
	}
}