package com.gdavidpb.tuindice.assistant.data.mapper

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantInsightAction
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantInsightSection
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantTermInsight
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityKind
import com.gdavidpb.tuindice.chatcontract.context.EntityRef
import com.gdavidpb.tuindice.chatcontract.insights.TermInsight

internal fun TermInsight.toAssistantTermInsight(): AssistantTermInsight {
	return AssistantTermInsight(
		id = id,
		termKey = termKey,
		kind = kind,
		title = title,
		body = body,
		entityRefs = entityRefs.map { ref -> ref.toAssistantEntityRef() },
		sections = sections.map { section ->
			AssistantInsightSection(title = section.title, text = section.text)
		},
		nextActions = nextActions.map { action ->
			AssistantInsightAction(
				target = action.target.toAssistantNavTarget(),
				label = action.label,
				entityRefs = action.entityRefs.map { ref -> ref.toAssistantEntityRef() }
			)
		},
		createdAtMillis = createdAtMillis
	)
}

private fun EntityRef.toAssistantEntityRef(): AssistantEntityRef {
	return AssistantEntityRef(kind = kind.toAssistantEntityKind(), id = id)
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

private fun NavTarget.toAssistantNavTarget(): AssistantNavTarget {
	return when (this) {
		NavTarget.SUMMARY -> AssistantNavTarget.SUMMARY
		NavTarget.STATISTICS -> AssistantNavTarget.STATISTICS
		NavTarget.SHARE_TERM -> AssistantNavTarget.SHARE_TERM
		NavTarget.RECORD -> AssistantNavTarget.RECORD
		NavTarget.RECORD_PROJECTION -> AssistantNavTarget.RECORD_PROJECTION
		NavTarget.RECORD_SCHEDULE -> AssistantNavTarget.RECORD_SCHEDULE
		NavTarget.RECORD_CREATE_TERM -> AssistantNavTarget.RECORD_CREATE_TERM
		NavTarget.EVALUATIONS -> AssistantNavTarget.EVALUATIONS
		NavTarget.EVALUATIONS_ADD -> AssistantNavTarget.EVALUATIONS_ADD
		NavTarget.EVALUATIONS_DETAIL -> AssistantNavTarget.EVALUATIONS_DETAIL
		NavTarget.PENSUM -> AssistantNavTarget.PENSUM
		NavTarget.SUBJECTS_SEARCH -> AssistantNavTarget.SUBJECTS_SEARCH
		NavTarget.SUBJECTS_DETAIL -> AssistantNavTarget.SUBJECTS_DETAIL
		NavTarget.ENROLLMENT_PROOF -> AssistantNavTarget.ENROLLMENT_PROOF
		NavTarget.ABOUT -> AssistantNavTarget.ABOUT
		NavTarget.SIGN_IN -> AssistantNavTarget.SIGN_IN
		NavTarget.UPDATE_PASSWORD -> AssistantNavTarget.UPDATE_PASSWORD
	}
}
