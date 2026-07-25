package com.gdavidpb.tuindice.pensum.domain.engine

import com.gdavidpb.tuindice.academiccore.domain.engine.AcademicPensumStatusEngine
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumGraph
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumNodeStatus
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumProgress
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicPensumSnapshot
import com.gdavidpb.tuindice.pensum.domain.model.PensumGraph
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeStatus
import com.gdavidpb.tuindice.pensum.domain.model.PensumNodeType
import com.gdavidpb.tuindice.pensum.domain.model.PensumProgress
import com.gdavidpb.tuindice.pensum.domain.model.PensumRelationshipType

class PensumStatusEngine(
	private val academicPensumStatusEngine: AcademicPensumStatusEngine = AcademicPensumStatusEngine()
) {
	fun resolve(
		pensum: PensumGraph,
		academicSnapshot: AcademicPensumSnapshot
	): PensumProgress {
		return academicPensumStatusEngine.resolve(
			pensum = pensum.toAcademicPensumGraph(),
			academicSnapshot = academicSnapshot
		).toPensumProgress()
	}
}

private fun PensumGraph.toAcademicPensumGraph(): AcademicPensumGraph {
	return AcademicPensumGraph(
		nodes = nodes.map { node -> node.toAcademicPensumNode() },
		edges = edges.map { edge -> edge.toAcademicPensumEdge() }
	)
}

private fun PensumGraph.Node.toAcademicPensumNode(): AcademicPensumGraph.Node {
	return AcademicPensumGraph.Node(
		id = id,
		nodeType = when (nodeType) {
			PensumNodeType.COURSE -> AcademicPensumGraph.NodeType.COURSE
			PensumNodeType.SLOT -> AcademicPensumGraph.NodeType.SLOT
		},
		subjectCode = subjectCode,
		credits = credits,
		fulfillmentRules = fulfillmentRules.map { rule ->
			AcademicPensumGraph.FulfillmentRule(
				ruleType = rule.ruleType,
				subjectCodes = rule.subjectCodes,
				subjectCodePrefixes = rule.subjectCodePrefixes,
				minCredits = rule.minCredits,
				minSubjects = rule.minSubjects
			)
		}
	)
}

private fun PensumGraph.Edge.toAcademicPensumEdge(): AcademicPensumGraph.Edge {
	return AcademicPensumGraph.Edge(
		fromNodeId = fromNodeId,
		toNodeId = toNodeId,
		relationshipType = when (relationshipType) {
			PensumRelationshipType.REQUIREMENT -> AcademicPensumGraph.RelationshipType.REQUIREMENT
			PensumRelationshipType.COREQUISITE -> AcademicPensumGraph.RelationshipType.COREQUISITE
		}
	)
}

private fun AcademicPensumProgress.toPensumProgress(): PensumProgress {
	return PensumProgress(
		approvedCredits = approvedCredits,
		nodeStatuses = nodeStatuses.mapValues { (_, status) -> status.toPensumNodeStatus() },
		nodeFulfillments = nodeFulfillments.mapValues { (_, fulfillment) ->
			PensumProgress.NodeFulfillment(
				subjectCode = fulfillment.subjectCode,
				subjectName = fulfillment.subjectName
			)
		}
	)
}

private fun AcademicPensumNodeStatus.toPensumNodeStatus(): PensumNodeStatus {
	return when (this) {
		AcademicPensumNodeStatus.APPROVED -> PensumNodeStatus.APPROVED
		AcademicPensumNodeStatus.CURRENT -> PensumNodeStatus.CURRENT
		AcademicPensumNodeStatus.AVAILABLE -> PensumNodeStatus.AVAILABLE
		AcademicPensumNodeStatus.BLOCKED -> PensumNodeStatus.BLOCKED
	}
}
