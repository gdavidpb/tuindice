package com.gdavidpb.tuindice.assistant.data.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantTermInsight
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.insights.TermInsight

internal fun TermInsight.toAssistantTermInsight(): AssistantTermInsight {
	return AssistantTermInsight(
		id = id,
		termKey = termKey,
		kind = kind,
		title = title,
		body = body,
		entityRefs = entityRefs.map { ref ->
			AssistantEntityRef(kind = ref.kind.toAssistantEntityKind(), id = ref.id)
		}
	)
}

private fun EntityKind.toAssistantEntityKind(): AssistantEntityKind {
	return when (this) {
		EntityKind.TERM -> AssistantEntityKind.TERM
		EntityKind.SUBJECT -> AssistantEntityKind.SUBJECT
		EntityKind.EVALUATION -> AssistantEntityKind.EVALUATION
		EntityKind.ATTEMPT -> AssistantEntityKind.ATTEMPT
		EntityKind.PENSUM -> AssistantEntityKind.PENSUM
		EntityKind.PLAN -> AssistantEntityKind.PLAN
	}
}
