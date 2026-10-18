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

// Every target of the chat has its own name here. A test asserts that the map covers all of them.
private val assistantNavTargets: Map<NavTarget, AssistantNavTarget> = mapOf(
	NavTarget.SUMMARY to AssistantNavTarget.SUMMARY,
	NavTarget.STATISTICS to AssistantNavTarget.STATISTICS,
	NavTarget.SHARE_TERM to AssistantNavTarget.SHARE_TERM,
	NavTarget.RECORD to AssistantNavTarget.RECORD,
	NavTarget.RECORD_PROJECTION to AssistantNavTarget.RECORD_PROJECTION,
	NavTarget.RECORD_SCHEDULE to AssistantNavTarget.RECORD_SCHEDULE,
	NavTarget.RECORD_CREATE_TERM to AssistantNavTarget.RECORD_CREATE_TERM,
	NavTarget.EVALUATIONS to AssistantNavTarget.EVALUATIONS,
	NavTarget.EVALUATIONS_ADD to AssistantNavTarget.EVALUATIONS_ADD,
	NavTarget.EVALUATIONS_DETAIL to AssistantNavTarget.EVALUATIONS_DETAIL,
	NavTarget.PENSUM to AssistantNavTarget.PENSUM,
	NavTarget.SUBJECTS_SEARCH to AssistantNavTarget.SUBJECTS_SEARCH,
	NavTarget.SUBJECTS_DETAIL to AssistantNavTarget.SUBJECTS_DETAIL,
	NavTarget.ENROLLMENT_PROOF to AssistantNavTarget.ENROLLMENT_PROOF,
	NavTarget.ABOUT to AssistantNavTarget.ABOUT,
	NavTarget.SIGN_IN to AssistantNavTarget.SIGN_IN,
	NavTarget.UPDATE_PASSWORD to AssistantNavTarget.UPDATE_PASSWORD
)

private fun NavTarget.toAssistantNavTarget(): AssistantNavTarget {
	return assistantNavTargets.getValue(this)
}
