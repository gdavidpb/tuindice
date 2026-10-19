package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardItem
import com.gdavidpb.tuindice.assistant.presentation.model.IntentCardStatus
import com.gdavidpb.tuindice.assistant.presentation.model.IntentDraftItem
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantChatTexts
import com.gdavidpb.tuindice.chatcontract.intent.AddEvaluationsArgs
import com.gdavidpb.tuindice.chatcontract.intent.EvaluationDraft
import com.gdavidpb.tuindice.chatcontract.intent.Intent
import com.gdavidpb.tuindice.chatcontract.intent.IntentArgs
import com.gdavidpb.tuindice.chatcontract.intent.IntentRisk
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

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
		status = status
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

// The line under a row: its type, its date and its maximum grade, as the student reads them.
private suspend fun EvaluationDraft.detail(texts: AssistantChatTexts): String {
	val date = dateMillis?.toDayMonthYear() ?: texts.undated()
	val parts = listOf(texts.evaluationTypeName(evaluationType), date, texts.maxGrade(maxGrade))

	return parts.joinToString(separator = " · ")
}

// dd/MM/yyyy in the time zone of the device.
private fun Long.toDayMonthYear(): String {
	val date = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.currentSystemDefault()).date

	return "${date.day.twoDigits()}/${(date.month.ordinal + 1).twoDigits()}/${date.year}"
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')
