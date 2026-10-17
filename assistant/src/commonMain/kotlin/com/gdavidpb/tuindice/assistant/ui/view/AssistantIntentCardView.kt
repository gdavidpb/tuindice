package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardStatus
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags

// An intent the agent proposed: an embedded card that turns into its receipt once it is resolved.
@Composable
fun AssistantIntentCardView(
	card: IntentCardItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Intent.card(card.intentId))) {
		Text(
			text = card.title,
			style = MaterialTheme.typography.titleMedium,
			color = MaterialTheme.colorScheme.onSurface
		)
		when (val status = card.status) {
			is IntentCardStatus.Resolved -> AssistantIntentReceiptView(
				intentId = card.intentId,
				outcome = status.outcome
			)

			IntentCardStatus.Open, IntentCardStatus.Running -> AssistantIntentDetailsView(
				card = card,
				onAction = onAction
			)
		}
	}
}
