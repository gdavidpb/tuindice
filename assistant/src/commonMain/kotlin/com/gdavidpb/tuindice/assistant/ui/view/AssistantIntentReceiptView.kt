package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
import com.gdavidpb.tuindice.chatcontract.intent.OutcomeKind
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_intent_outcome_cancelled
import tuindice.assistant.generated.resources.assistant_intent_outcome_confirmed
import tuindice.assistant.generated.resources.assistant_intent_outcome_conflict
import tuindice.assistant.generated.resources.assistant_intent_outcome_delegated
import tuindice.assistant.generated.resources.assistant_intent_outcome_expired
import tuindice.assistant.generated.resources.assistant_intent_outcome_failed
import tuindice.assistant.generated.resources.assistant_intent_outcome_queued
import tuindice.assistant.generated.resources.assistant_intent_outcome_rejected

// A resolved intent: its outcome in one line, and no actions.
@Composable
fun AssistantIntentReceiptView(
	intentId: String,
	outcome: OutcomeKind,
	modifier: Modifier = Modifier
) {
	Text(
		text = stringResource(outcome.labelResource()),
		modifier = modifier.testTag(AssistantUiTags.Intent.receipt(intentId)),
		style = MaterialTheme.typography.bodyLarge,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)
}

private fun OutcomeKind.labelResource(): StringResource =
	when (this) {
		OutcomeKind.CONFIRMED -> Res.string.assistant_intent_outcome_confirmed
		OutcomeKind.QUEUED -> Res.string.assistant_intent_outcome_queued
		OutcomeKind.CONFLICT -> Res.string.assistant_intent_outcome_conflict
		OutcomeKind.REJECTED -> Res.string.assistant_intent_outcome_rejected
		OutcomeKind.CANCELLED -> Res.string.assistant_intent_outcome_cancelled
		OutcomeKind.EXPIRED -> Res.string.assistant_intent_outcome_expired
		OutcomeKind.FAILED -> Res.string.assistant_intent_outcome_failed
		OutcomeKind.DELEGATED -> Res.string.assistant_intent_outcome_delegated
	}
