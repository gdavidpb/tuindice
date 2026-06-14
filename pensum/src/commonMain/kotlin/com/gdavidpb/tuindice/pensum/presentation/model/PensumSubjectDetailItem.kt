package com.gdavidpb.tuindice.pensum.presentation.model

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
	val blockingReasons: List<PensumSubjectRelationItem> = emptyList()
)
