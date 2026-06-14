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
		val fulfillmentRules: List<FulfillmentRule>
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
		val minSubjects: Int?
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
