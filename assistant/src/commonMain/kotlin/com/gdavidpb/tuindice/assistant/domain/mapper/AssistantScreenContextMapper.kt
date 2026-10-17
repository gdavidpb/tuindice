package com.gdavidpb.tuindice.assistant.domain.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.chatcontract.context.ScreenContext
import com.gdavidpb.tuindice.chatcontract.context.ScreenId

// The contract's defaults for the locale and the time zone of a screen context.
private const val CONTEXT_LOCALE = "es-VE"
private const val CONTEXT_TIME_ZONE = "America/Caracas"

internal fun AssistantScreenContext.toScreenContext(): ScreenContext {
	return ScreenContext(
		screen = screen.toScreenId(),
		entities = entities.map { entity -> entity.toEntityRef() },
		visibleFields = visibleFields,
		filters = filters,
		locale = CONTEXT_LOCALE,
		timeZone = CONTEXT_TIME_ZONE
	)
}

private fun AssistantEntityRef.toEntityRef(): EntityRef {
	return EntityRef(kind = kind.toEntityKind(), id = id)
}

private fun AssistantScreen.toScreenId(): ScreenId {
	return when (this) {
		AssistantScreen.SUMMARY -> ScreenId.SUMMARY
		AssistantScreen.STATISTICS -> ScreenId.STATISTICS
		AssistantScreen.RECORD -> ScreenId.RECORD
		AssistantScreen.PENSUM -> ScreenId.PENSUM
		AssistantScreen.EVALUATIONS -> ScreenId.EVALUATIONS
		AssistantScreen.EVALUATION_DETAIL -> ScreenId.EVALUATION_DETAIL
		AssistantScreen.SUBJECT_DETAIL -> ScreenId.SUBJECT_DETAIL
		AssistantScreen.SUBJECT_SEARCH -> ScreenId.SUBJECT_SEARCH
		AssistantScreen.ABOUT -> ScreenId.ABOUT
	}
}

private fun AssistantEntityKind.toEntityKind(): EntityKind {
	return when (this) {
		AssistantEntityKind.TERM -> EntityKind.TERM
		AssistantEntityKind.SUBJECT -> EntityKind.SUBJECT
		AssistantEntityKind.EVALUATION -> EntityKind.EVALUATION
		AssistantEntityKind.ATTEMPT -> EntityKind.ATTEMPT
		AssistantEntityKind.PENSUM -> EntityKind.PENSUM
		AssistantEntityKind.PLAN -> EntityKind.PLAN
	}
}
