package com.gdavidpb.tuindice.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef

// The chat's name of each target of the report, which is the target's own name: the report's actions reach the screens
// through the same jumps the chat uses. `everyTargetOfTheReportOpensItsChatScreen` checks that every target has one, in
// both enums. A `when` over the targets would be exhaustive too, but it is over the complexity threshold of detekt.
internal fun AssistantNavTarget.toChatNavTarget(): NavTarget = NavTarget.valueOf(name)

internal fun AssistantEntityRef.toChatEntityRef(): EntityRef {
	return EntityRef(
		kind = when (kind) {
			AssistantEntityKind.TERM -> EntityKind.TERM
			AssistantEntityKind.SUBJECT -> EntityKind.SUBJECT
			AssistantEntityKind.EVALUATION -> EntityKind.EVALUATION
			AssistantEntityKind.ATTEMPT -> EntityKind.ATTEMPT
			AssistantEntityKind.PENSUM -> EntityKind.PENSUM
			AssistantEntityKind.PLAN -> EntityKind.PLAN
		},
		id = id
	)
}
