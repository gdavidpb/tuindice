package com.gdavidpb.tuindice.pensum.domain.model

data class PensumGraph(
	val id: String,
	val careerCode: Int,
	val careerName: String,
	val year: Int,
	val modalityId: String,
	val modalityName: String,
	val totalCredits: Int,
	val canvas: Canvas,
	val terms: List<Term>,
	val nodes: List<Node>,
	val edges: List<Edge>
) {
	data class Canvas(
		val width: Double,
		val height: Double
	)

	data class Term(
		val id: String,
		val label: String,
		val x: Double,
		val width: Double
	)

	data class Node(
		val id: String,
		val nodeType: PensumNodeType,
		val displayCode: String,
		val subjectCode: String?,
		val name: String,
		val credits: Int,
		val category: String,
		val termId: String,
		val x: Double,
		val y: Double,
		val width: Double,
		val height: Double,
		val fulfillmentRules: List<FulfillmentRule>
	)

	data class FulfillmentRule(
		val id: String,
		val ruleType: String,
		val subjectCodes: List<String>,
		val subjectCodePrefixes: List<String>,
		val minCredits: Int?,
		val minSubjects: Int?
	)

	data class Edge(
		val id: String,
		val fromNodeId: String,
		val toNodeId: String,
		val relationshipType: PensumRelationshipType,
		val points: List<Point>
	)

	data class Point(
		val x: Double,
		val y: Double
	)
}
