package com.gdavidpb.tuindice.enrollmentproof.ui.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentProofFetchingSheet(
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
		sheetState = nonDismissSheetState,
		onDismissRequest = onDismissRequest,
		properties = ModalBottomSheetProperties(shouldDismissOnBackPress = false)
	) {
		Box(
			modifier = Modifier.fillMaxWidth(),
			contentAlignment = Alignment.Center
		) {
			loadingContent()
		}

		Text(
			modifier = Modifier
				.align(Alignment.CenterHorizontally)
				.padding(bottom = 24.dp),
			text = messageText,
			style = MaterialTheme.typography.titleLarge
		)
	}
}
