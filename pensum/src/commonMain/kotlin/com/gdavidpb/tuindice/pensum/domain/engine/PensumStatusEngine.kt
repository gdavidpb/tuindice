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
			.filter { attempt -> attempt.termKind == TermKind.CURRENT }
		val approvedSubjectCodes = approvedAttempts.map { attempt -> attempt.subjectCode }.toSet()
		val currentSubjectCodes = currentAttempts.map { attempt -> attempt.subjectCode }.toSet()

		val approvedNodeIds = pensum.nodes
			.filter { node -> node.isApproved(approvedSubjectCodes) }
			.map(PensumGraph.Node::id)
			.toSet()
		val currentNodeIds = pensum.nodes
			.filter { node -> node.id !in approvedNodeIds && node.isCurrent(currentSubjectCodes) }
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

	private fun PensumGraph.Node.isApproved(approvedSubjectCodes: Set<String>): Boolean {
		return when (nodeType) {
			PensumNodeType.COURSE -> subjectCode != null && subjectCode in approvedSubjectCodes
			PensumNodeType.SLOT -> false
		}
	}

	private fun PensumGraph.Node.isCurrent(currentSubjectCodes: Set<String>): Boolean {
		return when (nodeType) {
			PensumNodeType.COURSE -> subjectCode != null && subjectCode in currentSubjectCodes
			PensumNodeType.SLOT -> false
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
}
