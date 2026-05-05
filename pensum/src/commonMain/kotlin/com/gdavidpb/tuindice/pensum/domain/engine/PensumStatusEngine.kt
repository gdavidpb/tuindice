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
			.filter { attempt -> attempt.outcome == AttemptOutcome.APPROVED }
		val currentAttempts = academicSnapshot.attempts
			.filter { attempt -> attempt.termKind == TermKind.OFFICIAL_CURRENT }
		val approvedSubjectCodes = approvedAttempts.map { attempt -> attempt.subjectCode }.toSet()
		val currentSubjectCodes = currentAttempts.map { attempt -> attempt.subjectCode }.toSet()

		val approvedNodeIds = pensum.nodes
			.filter { node -> node.isApproved(approvedAttempts, approvedSubjectCodes) }
			.map(PensumGraph.Node::id)
			.toSet()
		val currentNodeIds = pensum.nodes
			.filter { node -> node.id !in approvedNodeIds && node.isCurrent(currentAttempts, currentSubjectCodes) }
			.map(PensumGraph.Node::id)
			.toSet()

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
			nodeStatuses = statuses
		)
	}

	private fun PensumGraph.Node.isApproved(
		approvedAttempts: List<AcademicPensumSnapshot.Attempt>,
		approvedSubjectCodes: Set<String>
	): Boolean {
		return when (nodeType) {
			PensumNodeType.COURSE -> subjectCode != null && subjectCode in approvedSubjectCodes
			PensumNodeType.SLOT -> fulfillmentRules.any { rule -> rule.isFulfilledBy(approvedAttempts) }
		}
	}

	private fun PensumGraph.Node.isCurrent(
		currentAttempts: List<AcademicPensumSnapshot.Attempt>,
		currentSubjectCodes: Set<String>
	): Boolean {
		return when (nodeType) {
			PensumNodeType.COURSE -> subjectCode != null && subjectCode in currentSubjectCodes
			PensumNodeType.SLOT -> fulfillmentRules.any { rule -> rule.isFulfilledBy(currentAttempts) }
		}
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

	private fun PensumGraph.FulfillmentRule.isFulfilledBy(
		attempts: List<AcademicPensumSnapshot.Attempt>
	): Boolean {
		val matchingAttempts = attempts.filter { attempt -> matches(attempt.subjectCode) }
		val subjectCount = matchingAttempts.map { attempt -> attempt.subjectCode }.distinct().size
		val credits = matchingAttempts
			.distinctBy(AcademicPensumSnapshot.Attempt::subjectCode)
			.sumOf(AcademicPensumSnapshot.Attempt::credits)

		return subjectCount >= (minSubjects ?: 1) && credits >= (minCredits ?: 0)
	}

	private fun PensumGraph.FulfillmentRule.matches(subjectCode: String): Boolean {
		return subjectCode in subjectCodes || subjectCodePrefixes.any { prefix -> subjectCode.startsWith(prefix) }
	}
}
