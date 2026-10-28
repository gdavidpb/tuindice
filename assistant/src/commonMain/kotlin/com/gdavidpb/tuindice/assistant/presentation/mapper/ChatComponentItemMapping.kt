package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.model.ChatComponentItem
import com.gdavidpb.tuindice.assistant.presentation.model.ChatFallbackItem
import com.gdavidpb.tuindice.chatcontract.component.ChatComponent
import com.gdavidpb.tuindice.chatcontract.component.ComponentDisplay

// The item a component is drawn from: the first of the families that takes it, and a component of none of them shows
// the text it carries. Each family maps its own components in ComponentFamilyMapping.kt.
internal fun ChatComponent.toChatComponentItem(
	nowMillis: Long,
	weeks: EvaluationWeeksContext? = null
): ChatComponentItem {
	return toSistemaItem(nowMillis)
		?: toRecordItem(nowMillis)
		?: toAcademicItem(weeks)
		?: toPensumPlanItem()
		?: ChatFallbackItem(id = id, opensSheet = opensSheet(), fallbackText = fallbackText)
}

// Whether the component opens on a sheet of its own.
internal fun ChatComponent.opensSheet(): Boolean = display == ComponentDisplay.SHEET
