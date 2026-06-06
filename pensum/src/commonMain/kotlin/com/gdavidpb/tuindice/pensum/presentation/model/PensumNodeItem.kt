package com.gdavidpb.tuindice.pensum.presentation.model

data class PensumNodeItem(
	val id: String,
	val displayCode: String,
	val subjectCode: String?,
	val name: String,
	val displayName: String,
	val credits: Int,
	val creditsText: String,
	val termId: String,
	val x: Double,
	val y: Double,
	val width: Double,
	val height: Double,
	val visualStyle: PensumNodeVisualStyle,
	val status: PensumNodeStatusDisplay,
	val subjectStatsCode: String?,
	val fulfilledSubject: PensumFulfilledSubjectItem?,
	val detail: PensumSubjectDetailItem
) {
	val isCurrent: Boolean get() = status.type == PensumNodeStatusType.CURRENT
	val isApproved: Boolean get() = status.type == PensumNodeStatusType.APPROVED
	val isBlocked: Boolean get() = status.type == PensumNodeStatusType.BLOCKED
	val hasSubjectStatsAction: Boolean get() = subjectStatsCode != null
}
