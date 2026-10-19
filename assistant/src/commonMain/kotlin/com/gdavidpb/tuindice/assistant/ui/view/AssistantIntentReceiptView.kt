package com.gdavidpb.tuindice.assistant.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.assistant.presentation.model.AssistantIntentOutcome
import com.gdavidpb.tuindice.assistant.ui.AssistantUiTags
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
	outcome: AssistantIntentOutcome,
	modifier: Modifier = Modifier
) {
	Text(
		text = stringResource(outcome.labelResource()),
		modifier = modifier.testTag(AssistantUiTags.Intent.receipt(intentId)),
		style = MaterialTheme.typography.bodyLarge,
		color = MaterialTheme.colorScheme.onSurfaceVariant
	)
}

private fun AssistantIntentOutcome.labelResource(): StringResource =
	when (this) {
		AssistantIntentOutcome.CONFIRMED -> Res.string.assistant_intent_outcome_confirmed
		AssistantIntentOutcome.QUEUED -> Res.string.assistant_intent_outcome_queued
		AssistantIntentOutcome.CONFLICT -> Res.string.assistant_intent_outcome_conflict
		AssistantIntentOutcome.REJECTED -> Res.string.assistant_intent_outcome_rejected
		AssistantIntentOutcome.CANCELLED -> Res.string.assistant_intent_outcome_cancelled
		AssistantIntentOutcome.EXPIRED -> Res.string.assistant_intent_outcome_expired
		AssistantIntentOutcome.FAILED -> Res.string.assistant_intent_outcome_failed
		AssistantIntentOutcome.DELEGATED -> Res.string.assistant_intent_outcome_delegated
	}
