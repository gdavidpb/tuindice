package com.gdavidpb.tuindice.enrollmentproof.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.enrollmentproof.ui.EnrollmentProofUiTags

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentProofFetchingSheet(
	cancelText: String,
	messageText: String,
	onDismissRequest: () -> Unit,
	loadingContent: @Composable () -> Unit
) {
	val nonDismissSheetState = rememberModalBottomSheetState(
		confirmValueChange = { sheetValue ->
			sheetValue != SheetValue.Hidden
		}
	)

	ModalBottomSheet(
		modifier = Modifier.testTag(EnrollmentProofUiTags.FetchingSheet),
		sheetState = nonDismissSheetState,
		onDismissRequest = onDismissRequest,
		properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)
	) {
		Box(
			modifier = Modifier
				.testTag(EnrollmentProofUiTags.FetchingLoadingContainer)
				.fillMaxWidth(),
			contentAlignment = Alignment.Center
		) {
			loadingContent()
		}

		Text(
			modifier = Modifier
				.testTag(EnrollmentProofUiTags.FetchingMessage)
				.align(Alignment.CenterHorizontally)
				.padding(bottom = 16.dp),
			text = messageText,
			style = MaterialTheme.typography.titleLarge
		)

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(start = 24.dp, top = 8.dp, end = 24.dp),
			horizontalArrangement = Arrangement.End
		) {
			OutlinedButton(
				modifier = Modifier.testTag(EnrollmentProofUiTags.FetchingCancelButton),
				onClick = onDismissRequest,
				border = null
			) {
				Text(text = cancelText)
			}
		}
	}
}
