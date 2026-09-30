package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumProgress
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSlotEligibility

/**
 * Answers, for a subject that is not a fixed course of [pensum], whether approving it would count
 * toward one of its slots given the student's [progress]. Built once per pensum and progress, then
 * asked per subject, so a search can label every result without re-resolving the pensum.
 *
 * It agrees with [AcademicPensumStatusEngine] by construction: the engine assigns approved
 * attempts to slots greedily in node order, so a newly approved subject lands on the first slot
 * that accepts it and is not approved yet. A slot being filled by a current attempt counts as open,
 * since that attempt may still fail and an approved subject would take the slot first.
 */
class AcademicPensumSlotEligibilityResolver(
	pensum: AcademicPensumGraph,
	private val progress: AcademicPensumProgress
) {
	private val fixedCourseSubjectCodes = pensum.fixedCourseSubjectCodes()
	private val slotNodes = pensum.nodes.filter { node -> node.nodeType == AcademicPensumGraph.NodeType.SLOT }

	fun eligibilityOf(subjectCode: String, credits: Int): AcademicPensumSlotEligibility {
		val code = subjectCode.normalizedPensumSubjectCodeOrNull()
		val acceptingSlots = if (code == null || code in fixedCourseSubjectCodes) {
			emptyList()
		} else {
			slotNodes.filter { node -> node.acceptsSubject(subjectCode = code, credits = credits) }
		}
		val openSlot = acceptingSlots.firstOrNull { node ->
			progress.nodeStatuses[node.id] != AcademicPensumNodeStatus.APPROVED
		}

		return when {
			openSlot != null -> AcademicPensumSlotEligibility.CountsTowardSlot(slotKind = openSlot.slotKind())
			acceptingSlots.isNotEmpty() -> AcademicPensumSlotEligibility.SlotsFilled(
				slotKind = acceptingSlots.first().slotKind()
			)
			else -> AcademicPensumSlotEligibility.NotEligible
		}
	}
}
