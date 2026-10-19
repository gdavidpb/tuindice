package com.gdavidpb.tuindice.assistant.ui.dialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import com.gdavidpb.tuindice.assistant.domain.model.AssistantInputLimits
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_cancel
import tuindice.assistant.generated.resources.assistant_chat_appeal_counter
import tuindice.assistant.generated.resources.assistant_chat_appeal_placeholder
import tuindice.assistant.generated.resources.assistant_chat_appeal_send
import tuindice.assistant.generated.resources.assistant_chat_appeal_title

private const val APPEAL_MIN_LINES = 3

// The text the student writes to appeal a restriction, at most AssistantInputLimits.APPEAL_MAX_LENGTH characters.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantAppealDialog(
	onAppeal: (text: String) -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
	val appealText = remember { mutableStateOf("") }
	val maxLength = AssistantInputLimits.APPEAL_MAX_LENGTH

	ConfirmationDialog(
		sheetState = sheetState,
		titleText = stringResource(Res.string.assistant_chat_appeal_title),
		positiveText = stringResource(Res.string.assistant_chat_appeal_send),
		negativeText = stringResource(Res.string.assistant_cancel),
		positiveEnabled = appealText.value.isNotBlank(),
		positiveButtonTestTag = AssistantUiTags.AppealSend,
		onPositiveClick = { onAppeal(appealText.value) },
			scrollable = true,
		onDismissRequest = onDismissRequest
	) {
		OutlinedTextField(
			value = appealText.value,
			onValueChange = { next -> if (next.length <= maxLength) appealText.value = next },
			modifier = Modifier.fillMaxWidth().testTag(AssistantUiTags.AppealText),
			placeholder = { Text(text = stringResource(Res.string.assistant_chat_appeal_placeholder)) },
			minLines = APPEAL_MIN_LINES
		)
		Text(
			modifier = Modifier.fillMaxWidth().testTag(AssistantUiTags.AppealCounter),
			text = stringResource(Res.string.assistant_chat_appeal_counter, appealText.value.length, maxLength),
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			textAlign = TextAlign.End
		)
	}
}
