package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardStatus
import com.gdavidpb.tuindice.assistant.presentation.model.IntentDraftItem
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantChatTexts
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.intent.AddEvaluationsArgs
import com.gdavidpb.tuindice.chatcontract.intent.EvaluationDraft
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.IntentRisk
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_intent_draft_detail

internal suspend fun Intent.toIntentCardItem(
	texts: AssistantChatTexts,
	status: IntentCardStatus,
	selections: Map<Pair<String, String>, Boolean>
): IntentCardItem {
	return IntentCardItem(
		intentId = id,
		title = title,
		effect = effect,
		isDestructive = risk == IntentRisk.DESTRUCTIVE,
		usesCredential = risk == IntentRisk.CREDENTIAL,
		confirmLabel = confirmLabel,
		cancelLabel = cancelLabel,
		drafts = args.toDraftItems(texts, id, selections),
		expiresAtMillis = expiresAtMillis,
		status = status,
		runningLabel = if (status == IntentCardStatus.Running) texts.intentConfirming() else null
	)
}

// Only a batch of evaluations has rows the student can unmark. A row keeps the choice the student made on it.
private suspend fun IntentArgs.toDraftItems(
	texts: AssistantChatTexts,
	intentId: String,
	selections: Map<Pair<String, String>, Boolean>
): List<IntentDraftItem> {
	val batch = this as? AddEvaluationsArgs ?: return emptyList()

	return batch.items.map { item ->
		IntentDraftItem(
			mutationId = item.mutationId,
			label = item.reference,
			detail = item.detail(texts),
			selected = selections[intentId to item.mutationId] ?: item.selected
		)
	}
}

// The line under a row: its type, its date and its maximum grade, as the student reads them. The date is the one
// formatDate writes for a day, so it reads as the rest of the app does.
private suspend fun EvaluationDraft.detail(texts: AssistantChatTexts): UiText {
	val date = dateMillis?.formatDate(DateTextStyle.DAY_MONTH_YEAR) ?: UiText.Raw(texts.undated())

	return UiText.Resource(
		Res.string.assistant_intent_draft_detail,
		listOf(UiText.Raw(texts.evaluationTypeName(evaluationType)), date, UiText.Raw(texts.maxGrade(maxGrade)))
	)
}
