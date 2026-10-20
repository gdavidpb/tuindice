package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.CooldownItem
import com.gdavidpb.tuindice.assistant.presentation.model.StillAppliesItem
import com.gdavidpb.tuindice.assistant.presentation.model.SupportCallItem
import com.gdavidpb.tuindice.assistant.presentation.model.SupportItem
import com.gdavidpb.tuindice.assistant.presentation.model.SupportLineItem
import com.gdavidpb.tuindice.assistant.presentation.model.SuspensionItem
import com.gdavidpb.tuindice.assistant.presentation.model.UsageNoticeItem
import com.gdavidpb.tuindice.assistant.presentation.model.WarningItem
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.component.CooldownComponent
import com.gdavidpb.tuindice.chatcontract.component.StillAppliesComponent
import com.gdavidpb.tuindice.chatcontract.component.SupportComponent
import com.gdavidpb.tuindice.chatcontract.component.SuspensionComponent
import com.gdavidpb.tuindice.chatcontract.component.UsageNoticeComponent
import com.gdavidpb.tuindice.chatcontract.component.WarningComponent
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_cooldown_until
import tuindice.assistant.generated.resources.assistant_support_call
import tuindice.assistant.generated.resources.assistant_suspension_open_ended
import tuindice.assistant.generated.resources.assistant_suspension_until
import tuindice.assistant.generated.resources.assistant_usage_day
import tuindice.assistant.generated.resources.assistant_usage_hour
import tuindice.assistant.generated.resources.assistant_usage_month
import tuindice.assistant.generated.resources.assistant_usage_resets

internal fun StillAppliesComponent.toStillAppliesItem(): StillAppliesItem {
	return StillAppliesItem(
		id = id,
		opensSheet = opensSheet(),
		memoryId = memoryId,
		statement = statement
	)
}

internal fun UsageNoticeComponent.toUsageNoticeItem(nowMillis: Long): UsageNoticeItem {
	return UsageNoticeItem(
		id = id,
		opensSheet = opensSheet(),
		hourText = UiText.Resource(Res.string.assistant_usage_hour, listOf(usage.remainingHour)),
		dayText = UiText.Resource(Res.string.assistant_usage_day, listOf(usage.remainingDay)),
		monthText = UiText.Resource(Res.string.assistant_usage_month, listOf(usage.remainingMonth)),
		resetsText = resetsAtMillis?.let { millis ->
			UiText.Resource(Res.string.assistant_usage_resets, listOf(millis.toChatDateText(nowMillis)))
		}
	)
}

internal fun CooldownComponent.toCooldownItem(nowMillis: Long): CooldownItem {
	return CooldownItem(
		id = id,
		opensSheet = opensSheet(),
		reasonText = reason.toReasonText(),
		untilText = UiText.Resource(Res.string.assistant_cooldown_until, listOf(untilMillis.toChatDateText(nowMillis))),
		appealable = appealable
	)
}

internal fun SuspensionComponent.toSuspensionItem(nowMillis: Long): SuspensionItem {
	val untilText = untilMillis?.let { millis ->
		UiText.Resource(Res.string.assistant_suspension_until, listOf(millis.toChatDateText(nowMillis)))
	} ?: UiText.Resource(Res.string.assistant_suspension_open_ended)

	return SuspensionItem(
		id = id,
		opensSheet = opensSheet(),
		reasonText = reason.toReasonText(),
		untilText = untilText,
		appealable = appealable
	)
}

internal fun SupportComponent.toSupportItem(): SupportItem {
	return SupportItem(
		id = id,
		opensSheet = opensSheet(),
		lines = lines.map { line ->
			SupportLineItem(
				label = line.label,
				call = line.phone?.let { phone ->
					SupportCallItem(
						text = UiText.Resource(Res.string.assistant_support_call, listOf(phone)),
						telUri = phone.toTelUri()
					)
				},
				pageUri = line.url
			)
		}
	)
}

internal fun WarningComponent.toWarningItem(): WarningItem {
	return WarningItem(
		id = id,
		opensSheet = opensSheet(),
		reasonText = reason.toReasonText()
	)
}

// The address a phone is dialed at: its digits, and the plus sign of an international number.
private fun String.toTelUri(): String = "tel:" + filter { it.isDigit() || it == '+' }
