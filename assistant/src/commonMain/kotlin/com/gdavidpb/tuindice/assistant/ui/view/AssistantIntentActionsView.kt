package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardStatus
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// At most two actions: the secondary cancel, and the primary confirm, which turns to the error colors when the intent
// is destructive. Both are disabled while the intent runs.
@Composable
fun AssistantIntentActionsView(
	card: IntentCardItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val isEnabled = card.status == IntentCardStatus.Open
	val confirmColors = if (card.isDestructive) {
		ButtonDefaults.buttonColors(
			containerColor = MaterialTheme.colorScheme.error,
			contentColor = MaterialTheme.colorScheme.onError
		)
	} else {
		ButtonDefaults.buttonColors()
	}

	Row(
		modifier = modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium, Alignment.End)
	) {
		OutlinedButton(
			onClick = { onAction(AssistantChat.Action.DismissIntent(card.intentId)) },
			enabled = isEnabled,
			modifier = Modifier.testTag(AssistantUiTags.Intent.cancel(card.intentId))
		) {
			Text(text = card.cancelLabel)
		}
		Button(
			onClick = { onAction(AssistantChat.Action.ConfirmIntent(card.intentId)) },
			enabled = isEnabled,
			colors = confirmColors,
			modifier = Modifier.testTag(AssistantUiTags.Intent.confirm(card.intentId))
		) {
			Text(text = card.confirmLabel)
		}
	}
}
