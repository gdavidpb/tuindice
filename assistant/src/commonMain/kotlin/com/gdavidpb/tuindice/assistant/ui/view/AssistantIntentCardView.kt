package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantChat
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardStatus
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.assistant.ui.component.common.CardTitleView
import com.gdavidpb.tuindice.assistant.ui.style.AssistantChatMotion
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.utils.extension.rememberLastNonNull

// An intent the agent proposed: an embedded card that turns into its receipt once it is resolved. When it resolves, the
// card eases to its new size and its status area crossfades from the actions to the receipt.
@Composable
fun AssistantIntentCardView(
	card: IntentCardItem,
	onAction: (AssistantChat.Action) -> Unit,
	modifier: Modifier = Modifier
) {
	val animationsEnabled = LocalTuIndiceAnimationsEnabled.current
	val sizeModifier = if (animationsEnabled) {
		Modifier.animateContentSize(animationSpec = tween(AssistantChatMotion.SmallEnterMillis))
	} else {
		Modifier
	}

	AssistantEmbeddedCardView(modifier = modifier.testTag(AssistantUiTags.Intent.card(card.intentId)).then(sizeModifier)) {
		CardTitleView(text = card.title)
		if (animationsEnabled) {
			// The receipt is read from the card that is current when it is drawn, and the actions are drawn from the last card
			// that was not resolved, so the actions fade out showing what they showed.
			val lastOpenCard = rememberLastNonNull(card.takeIf { it.status !is IntentCardStatus.Resolved })

			Crossfade(
				targetState = card.status is IntentCardStatus.Resolved,
				animationSpec = tween(AssistantChatMotion.SmallEnterMillis),
				label = "AssistantIntentStatus"
			) { isResolved ->
				if (isResolved) {
					IntentStatusView(card = card, onAction = onAction)
				} else {
					IntentStatusView(card = lastOpenCard ?: card, onAction = onAction)
				}
			}
		} else {
			IntentStatusView(card = card, onAction = onAction)
		}
	}
}

@Composable
private fun IntentStatusView(
	card: IntentCardItem,
	onAction: (AssistantChat.Action) -> Unit
) {
	when (val status = card.status) {
		is IntentCardStatus.Resolved -> AssistantIntentReceiptView(
			intentId = card.intentId,
			outcome = status.outcome,
			receipt = status.receipt
		)

		IntentCardStatus.Open, IntentCardStatus.Running -> AssistantIntentDetailsView(
			card = card,
			onAction = onAction
		)
	}
}
