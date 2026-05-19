package com.gdavidpb.tuindice.pensum.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.pensum.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeStatus
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeType
import com.gdavidpb.tuindice.pensum.domain.model.PensumProgress
import com.gdavidpb.tuindice.pensum.domain.model.PensumRelationshipType

class PensumStatusEngine {
	fun resolve(
		pensum: PensumGraph,
		academicSnapshot: AcademicPensumSnapshot
	): PensumProgress {
		val approvedAttempts = academicSnapshot.attempts
			.filter { attempt -> attempt.termKind != TermKind.SYNTHETIC }
			.filter { attempt -> attempt.outcome == AttemptOutcome.APPROVED }
			.sortedChronologically()
		val currentAttempts = academicSnapshot.attempts
			.filter { attempt -> attempt.termKind == TermKind.CURRENT && attempt.outcome != AttemptOutcome.APPROVED }
			.sortedChronologically()
		val approvedSubjectCodes = approvedAttempts.map { attempt -> attempt.normalizedSubjectCode() }.toSet()
		val currentSubjectCodes = currentAttempts.map { attempt -> attempt.normalizedSubjectCode() }.toSet()
		val fixedCourseSubjectCodes = pensum.nodes
			.filter { node -> node.nodeType == PensumNodeType.COURSE }
			.flatMap { node ->
				listOfNotNull(node.subjectCode.normalizedSubjectCodeOrNull()) +
					node.fulfillmentRules.flatMap { rule ->
						rule.subjectCodes.mapNotNull { code -> code.normalizedSubjectCodeOrNull() }
					}
			}
			.toSet()
		val slotNodes = pensum.nodes.filter { node -> node.nodeType == PensumNodeType.SLOT }

		val approvedCourseNodeIds = pensum.nodes
			.filter { node -> node.isApproved(approvedSubjectCodes) }
			.map(PensumGraph.Node::id)
			.toSet()
		val approvedSlotFulfillments = slotNodes.assignFulfillments(
			attempts = approvedAttempts.filter { attempt -> attempt.normalizedSubjectCode() !in fixedCourseSubjectCodes }
		)
		val approvedNodeIds = approvedCourseNodeIds + approvedSlotFulfillments.keys
		val currentCourseNodeIds = pensum.nodes
			.filter { node -> node.id !in approvedNodeIds && node.isCurrent(currentSubjectCodes) }
			.map(PensumGraph.Node::id)
			.toSet()
		val currentSlotFulfillments = slotNodes
			.filter { node -> node.id !in approvedNodeIds }
			.assignFulfillments(
				attempts = currentAttempts.filter { attempt -> attempt.normalizedSubjectCode() !in fixedCourseSubjectCodes }
			)
		val currentNodeIds = currentCourseNodeIds + currentSlotFulfillments.keys

		val statuses = pensum.nodes.associate { node ->
			val status = when {
				node.id in approvedNodeIds -> PensumNodeStatus.APPROVED
				node.id in currentNodeIds -> PensumNodeStatus.CURRENT
				node.isAvailable(pensum.edges, approvedNodeIds, currentNodeIds) -> PensumNodeStatus.AVAILABLE
				else -> PensumNodeStatus.BLOCKED
			}

			node.id to status
		}
		val approvedCredits = pensum.nodes
			.filter { node -> statuses[node.id] == PensumNodeStatus.APPROVED }
			.sumOf(PensumGraph.Node::credits)

		return PensumProgress(
			approvedCredits = approvedCredits,
			nodeStatuses = statuses,
			nodeFulfillments = approvedSlotFulfillments + currentSlotFulfillments
		)
	}

	private fun PensumGraph.Node.isApproved(approvedSubjectCodes: Set<String>): Boolean {
		return when (nodeType) {
			PensumNodeType.COURSE -> subjectCode.normalizedSubjectCodeOrNull() in approvedSubjectCodes
			PensumNodeType.SLOT -> false
		}
	}

	private fun PensumGraph.Node.isCurrent(currentSubjectCodes: Set<String>): Boolean {
		return when (nodeType) {
			PensumNodeType.COURSE -> subjectCode.normalizedSubjectCodeOrNull() in currentSubjectCodes
			PensumNodeType.SLOT -> false
		}
	}

	private fun List<PensumGraph.Node>.assignFulfillments(
		attempts: List<AcademicPensumSnapshot.Attempt>
	): Map<String, PensumProgress.NodeFulfillment> {
		val usedAttemptIds = mutableSetOf<String>()
		return mapNotNull { node ->
			val attempt = attempts.firstOrNull { attempt ->
				attempt.id !in usedAttemptIds && node.matchesAttempt(attempt)
			} ?: return@mapNotNull null
			usedAttemptIds += attempt.id
			node.id to PensumProgress.NodeFulfillment(
				subjectCode = attempt.normalizedSubjectCode(),
				subjectName = attempt.subjectName
			)
		}.toMap()
	}

	private fun PensumGraph.Node.matchesAttempt(attempt: AcademicPensumSnapshot.Attempt): Boolean {
		return fulfillmentRules.any { rule -> rule.matchesAttempt(attempt) }
	}

	private fun PensumGraph.FulfillmentRule.matchesAttempt(
		attempt: AcademicPensumSnapshot.Attempt
	): Boolean {
		if (minSubjects != null && minSubjects > 1) return false
		if (minCredits != null && attempt.credits < minCredits) return false

		val subjectCode = attempt.normalizedSubjectCode()
		val acceptedCodes = subjectCodes.mapNotNull { code -> code.normalizedSubjectCodeOrNull() }.toSet()
		val acceptedPrefixes = subjectCodePrefixes.mapNotNull { prefix -> prefix.normalizedSubjectCodeOrNull() }
		if (acceptedCodes.isEmpty() && acceptedPrefixes.isEmpty()) return false

		return subjectCode in acceptedCodes || acceptedPrefixes.any(subjectCode::startsWith)
	}

	private fun PensumGraph.Node.isAvailable(
		edges: List<PensumGraph.Edge>,
		approvedNodeIds: Set<String>,
		currentNodeIds: Set<String>
	): Boolean {
		val requirements = edges.filter { edge -> edge.toNodeId == id }
		return requirements.all { edge ->
			when (edge.relationshipType) {
				PensumRelationshipType.REQUIREMENT -> edge.fromNodeId in approvedNodeIds
				PensumRelationshipType.COREQUISITE -> edge.fromNodeId in approvedNodeIds || edge.fromNodeId in currentNodeIds
			}
		}
	}

	private fun List<AcademicPensumSnapshot.Attempt>.sortedChronologically(): List<AcademicPensumSnapshot.Attempt> {
		return sortedWith(
			compareBy<AcademicPensumSnapshot.Attempt> { attempt -> attempt.termOrder }
				.thenBy { attempt -> attempt.positionInTerm }
				.thenBy { attempt -> attempt.normalizedSubjectCode() }
				.thenBy { attempt -> attempt.id }
		)
	}

	private fun AcademicPensumSnapshot.Attempt.normalizedSubjectCode(): String {
		return subjectCode.normalizedSubjectCodeOrNull() ?: subjectCode.trim().uppercase()
	}

	private fun String?.normalizedSubjectCodeOrNull(): String? {
		return this?.trim()?.uppercase()?.takeIf(String::isNotBlank)
	}
}
