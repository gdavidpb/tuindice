package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import com.gdavidpb.tuindice.pensum.ui.model.PensumSubjectDetailNavigationTarget

@Composable
fun PensumSubjectExpandedDetailContent(
	node: PensumNodeItem,
	navigationOriginNodeId: String?,
	onRelatedSubjectClick: (PensumSubjectDetailNavigationTarget) -> Unit
) {
	val detail = node.detail

	Column(
		modifier = Modifier.fillMaxWidth(),
		verticalArrangement = Arrangement.spacedBy(14.dp)
	) {
		if (detail.fulfilledSubject != null) {
			PensumFulfilledSubjectSummary(
				fulfilledSubject = detail.fulfilledSubject,
				visualStyle = node.visualStyle
			)
		}

		val routeBeforeItems = detail.blockingReasons.ifEmpty { detail.requirements }
		if (routeBeforeItems.isNotEmpty() || detail.unlocks.isNotEmpty()) {
			PensumSubjectRouteContext(
				node = node,
				beforeItems = routeBeforeItems,
				beforeTestTag = if (detail.blockingReasons.isNotEmpty()) {
					PensumUiTags.SubjectDetailBlockingReasons
				} else {
					PensumUiTags.SubjectDetailRequirements
				},
				beforeRowTag = { nodeId ->
					if (detail.blockingReasons.isNotEmpty()) {
						PensumUiTags.subjectDetailBlockingReason(nodeId)
					} else {
						PensumUiTags.subjectDetailRequirement(nodeId)
					}
				},
				afterItems = detail.unlocks,
				navigationOriginNodeId = navigationOriginNodeId,
				onRelatedSubjectClick = onRelatedSubjectClick
			)
		}

		PensumSubjectCorequisiteSection(
			originNodeId = node.id,
			items = detail.corequisites,
			onRelatedSubjectClick = onRelatedSubjectClick
		)
	}
}
