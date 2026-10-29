package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardStatus
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_intent_uses_credential

// The part of an open or running intent: what it will do, the rows the student may unmark, and its actions. A card
// closed without an outcome keeps the first two and shows no actions (showsActions is false).
@Composable
fun AssistantIntentDetailsView(
	card: IntentCardItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier,
	showsActions: Boolean = true
) {
	val isOpen = card.status == IntentCardStatus.Open

	Column(
		modifier = modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		card.effect.forEach { line ->
			Text(
				text = buildAnnotatedString {
					withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(line.label) }
					append(" ")
					append(line.detail)
				},
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurface
			)
		}
		card.drafts.forEach { draft ->
			AssistantIntentDraftRowView(
				intentId = card.intentId,
				draft = draft,
				enabled = isOpen,
				onAction = onAction
			)
		}
		if (card.usesCredential) {
			Text(
				text = stringResource(Res.string.assistant_intent_uses_credential),
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		if (card.status == IntentCardStatus.Running) {
			card.runningLabel?.let { label ->
				Text(
					text = label,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			LinearProgressIndicator(
				modifier = Modifier.fillMaxWidth().testTag(AssistantUiTags.Intent.progress(card.intentId))
			)
		}
		if (showsActions) AssistantIntentActionsView(card = card, onAction = onAction)
	}
}
