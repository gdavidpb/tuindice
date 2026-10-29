package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.dialog.AssistantAppealDialog
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_chat_appeal_action

// The «Apelar» action of a restriction: it opens the appeal dialog and hands the written text to [onAppeal].
@Composable
fun AssistantAppealActionView(
	onAppeal: (text: String) -> Unit,
	modifier: Modifier = Modifier
) {
	val isDialogVisible = remember { mutableStateOf(false) }

	TextButton(
		onClick = { isDialogVisible.value = true },
		modifier = modifier.testTag(AssistantUiTags.AppealAction)
	) {
		Text(text = stringResource(Res.string.assistant_chat_appeal_action))
	}

	if (isDialogVisible.value) {
		AssistantAppealDialog(
			onAppeal = onAppeal,
			onDismissRequest = { isDialogVisible.value = false }
		)
	}
}
