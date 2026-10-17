package com.gdavidpb.tuindice.assistant.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.exposeTestTagsAsResourceId
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_cancel
import tuindice.assistant.generated.resources.assistant_memory_clear_all_confirm
import tuindice.assistant.generated.resources.assistant_memory_clear_all_title

// Asks before deleting everything the assistant remembers. «Eliminar» is the destructive answer, in the error colors.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearAssistantMemoryDialog(
	onConfirmClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()

	ModalBottomSheet(
		modifier = Modifier
			.exposeTestTagsAsResourceId()
			.testTag(AssistantUiTags.ClearAllDialog),
		sheetState = sheetState,
		onDismissRequest = onDismissRequest
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(start = TuIndiceSpacing.Dialog, end = TuIndiceSpacing.Dialog, bottom = TuIndiceSpacing.Dialog)
		) {
			Text(
				text = stringResource(Res.string.assistant_memory_clear_all_title),
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Bold
			)
			Row(
				modifier = Modifier.fillMaxWidth().padding(top = TuIndiceSpacing.Dialog),
				horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium, Alignment.End)
			) {
				OutlinedButton(
					onClick = onDismissRequest,
					modifier = Modifier.testTag(AssistantUiTags.ClearAllCancel)
				) {
					Text(text = stringResource(Res.string.assistant_cancel))
				}
				Button(
					onClick = onConfirmClick,
					colors = ButtonDefaults.buttonColors(
						containerColor = MaterialTheme.colorScheme.error,
						contentColor = MaterialTheme.colorScheme.onError
					),
					modifier = Modifier.testTag(AssistantUiTags.ClearAllConfirm)
				) {
					Text(text = stringResource(Res.string.assistant_memory_clear_all_confirm))
				}
			}
		}
	}
}
