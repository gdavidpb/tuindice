package com.gdavidpb.tuindice.pensum.presentation.model

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext

data class PensumSubjectDetailItem(
	val code: String,
	val name: String,
	val status: PensumNodeStatusDisplay,
	val termLabel: String?,
	val creditsText: String,
	val statsCode: String?,
	val fulfilledSubject: PensumFulfilledSubjectItem?,
	val requirements: List<PensumSubjectRelationItem> = emptyList(),
	val corequisites: List<PensumSubjectRelationItem> = emptyList(),
	val unlocks: List<PensumSubjectRelationItem> = emptyList(),
	val assistantContext: AssistantScreenContext = AssistantScreenContext(screen = AssistantScreen.SUBJECT_DETAIL)
)
