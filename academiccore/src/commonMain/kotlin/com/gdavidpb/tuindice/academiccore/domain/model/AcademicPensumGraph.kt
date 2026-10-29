package com.gdavidpb.tuindice.academiccore.domain.model

data class AcademicPensumGraph(
	val nodes: List<Node>,
	val edges: List<Edge>
) {
	data class Node(
		val id: String,
		val nodeType: NodeType,
		val subjectCode: String?,
		val credits: Int,
		val fulfillmentRules: List<FulfillmentRule>,
		// The pensum's own label for a SLOT (GENERAL_STUDIES, AREA_ELECTIVE...); see slotKind().
		val category: String? = null,
		// COURSE nodes sharing the same non-null group are alternatives of one requirement («one of
		// them»), e.g. MA1111 (regular) or MA1121 (honor) for Matemáticas I: approving (or taking)
		// any member approves (or makes current) the whole group, the siblings carry a fulfillment
		// with the attempt that did it, and the group's credits count once. Null: a node of its own.
		val alternativeGroup: String? = null
	)

	enum class NodeType {
		COURSE,
		SLOT
	}

	data class FulfillmentRule(
		val ruleType: String,
		val subjectCodes: List<String>,
		val subjectCodePrefixes: List<String>,
		val minCredits: Int?,
		val minSubjects: Int?,
		// GENERAL or ELECTIVE on a SUBJECT_ELIGIBILITY rule, whose codes the server already expanded.
		val slotEligibilityKind: String? = null
	)

	data class Edge(
		val fromNodeId: String,
		val toNodeId: String,
		val relationshipType: RelationshipType
	)

	enum class RelationshipType {
		REQUIREMENT,
		COREQUISITE
	}
}
