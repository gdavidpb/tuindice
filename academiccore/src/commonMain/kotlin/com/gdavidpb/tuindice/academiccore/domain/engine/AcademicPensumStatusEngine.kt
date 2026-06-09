package com.gdavidpb.tuindice.academiccore.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumProgress
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind

class AcademicPensumStatusEngine {
	fun resolve(
		pensum: AcademicPensumGraph,
		academicSnapshot: AcademicPensumSnapshot
	): AcademicPensumProgress {
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
			.filter { node -> node.nodeType == AcademicPensumGraph.NodeType.COURSE }
			.flatMap { node ->
				listOfNotNull(node.subjectCode.normalizedSubjectCodeOrNull()) +
					node.fulfillmentRules.flatMap { rule ->
						rule.subjectCodes.mapNotNull { code -> code.normalizedSubjectCodeOrNull() }
					}
			}
			.toSet()
		val slotNodes = pensum.nodes.filter { node -> node.nodeType == AcademicPensumGraph.NodeType.SLOT }

		val approvedCourseNodeIds = pensum.nodes
			.filter { node -> node.isApproved(approvedSubjectCodes) }
			.map(AcademicPensumGraph.Node::id)
			.toSet()
		val approvedSlotFulfillments = slotNodes.assignFulfillments(
			attempts = approvedAttempts.filter { attempt -> attempt.normalizedSubjectCode() !in fixedCourseSubjectCodes }
		)
		val approvedNodeIds = approvedCourseNodeIds + approvedSlotFulfillments.keys
		val currentCourseNodeIds = pensum.nodes
			.filter { node -> node.id !in approvedNodeIds && node.isCurrent(currentSubjectCodes) }
			.map(AcademicPensumGraph.Node::id)
			.toSet()
		val currentSlotFulfillments = slotNodes
			.filter { node -> node.id !in approvedNodeIds }
			.assignFulfillments(
				attempts = currentAttempts.filter { attempt -> attempt.normalizedSubjectCode() !in fixedCourseSubjectCodes }
			)
		val currentNodeIds = currentCourseNodeIds + currentSlotFulfillments.keys

		val statuses = pensum.nodes.associate { node ->
			val status = when {
				node.id in approvedNodeIds -> AcademicPensumNodeStatus.APPROVED
				node.id in currentNodeIds -> AcademicPensumNodeStatus.CURRENT
				node.isAvailable(pensum.edges, approvedNodeIds, currentNodeIds) -> AcademicPensumNodeStatus.AVAILABLE
				else -> AcademicPensumNodeStatus.BLOCKED
			}

			node.id to status
		}
		val approvedCredits = pensum.nodes
			.filter { node -> statuses[node.id] == AcademicPensumNodeStatus.APPROVED }
			.sumOf(AcademicPensumGraph.Node::credits)

		return AcademicPensumProgress(
			approvedCredits = approvedCredits,
			nodeStatuses = statuses,
			nodeFulfillments = approvedSlotFulfillments + currentSlotFulfillments
		)
	}

	private fun AcademicPensumGraph.Node.isApproved(approvedSubjectCodes: Set<String>): Boolean {
		return when (nodeType) {
			AcademicPensumGraph.NodeType.COURSE -> subjectCode.normalizedSubjectCodeOrNull() in approvedSubjectCodes
			AcademicPensumGraph.NodeType.SLOT -> false
		}
	}

	private fun AcademicPensumGraph.Node.isCurrent(currentSubjectCodes: Set<String>): Boolean {
		return when (nodeType) {
			AcademicPensumGraph.NodeType.COURSE -> subjectCode.normalizedSubjectCodeOrNull() in currentSubjectCodes
			AcademicPensumGraph.NodeType.SLOT -> false
		}
	}

	private fun List<AcademicPensumGraph.Node>.assignFulfillments(
		attempts: List<AcademicPensumSnapshot.Attempt>
	): Map<String, AcademicPensumProgress.NodeFulfillment> {
		val usedAttemptIds = mutableSetOf<String>()
		return mapNotNull { node ->
			val attempt = attempts.firstOrNull { attempt ->
				attempt.id !in usedAttemptIds && node.matchesAttempt(attempt)
			} ?: return@mapNotNull null
			usedAttemptIds += attempt.id
			node.id to AcademicPensumProgress.NodeFulfillment(
				subjectCode = attempt.normalizedSubjectCode(),
				subjectName = attempt.subjectName
			)
		}.toMap()
	}

	private fun AcademicPensumGraph.Node.matchesAttempt(attempt: AcademicPensumSnapshot.Attempt): Boolean {
		return fulfillmentRules.any { rule -> rule.matchesAttempt(attempt) }
	}

	private fun AcademicPensumGraph.FulfillmentRule.matchesAttempt(
		attempt: AcademicPensumSnapshot.Attempt
	): Boolean {
		if (ruleType == "GENERIC_ELECTIVE") return false
		if (minSubjects != null && minSubjects > 1) return false
		if (minCredits != null && attempt.credits < minCredits) return false

		val subjectCode = attempt.normalizedSubjectCode()
		val acceptedCodes = subjectCodes.mapNotNull { code -> code.normalizedSubjectCodeOrNull() }.toSet()
		val acceptedPrefixes = subjectCodePrefixes.mapNotNull { prefix -> prefix.normalizedSubjectCodeOrNull() }
		if (acceptedCodes.isEmpty() && acceptedPrefixes.isEmpty()) return false

		return subjectCode in acceptedCodes || acceptedPrefixes.any(subjectCode::startsWith)
	}

	private fun AcademicPensumGraph.Node.isAvailable(
		edges: List<AcademicPensumGraph.Edge>,
		approvedNodeIds: Set<String>,
		currentNodeIds: Set<String>
	): Boolean {
		val requirements = edges.filter { edge -> edge.toNodeId == id }
		return requirements.all { edge ->
			when (edge.relationshipType) {
				AcademicPensumGraph.RelationshipType.REQUIREMENT -> edge.fromNodeId in approvedNodeIds
				AcademicPensumGraph.RelationshipType.COREQUISITE ->
					edge.fromNodeId in approvedNodeIds || edge.fromNodeId in currentNodeIds
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
