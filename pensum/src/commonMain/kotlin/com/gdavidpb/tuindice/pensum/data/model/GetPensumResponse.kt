package com.gdavidpb.tuindice.pensum.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPensumResponse(
	@SerialName("selected_pensum_id") val selectedPensumId: String,
	@SerialName("inferred") val inferred: Boolean,
	@SerialName("pensums") val pensums: List<Pensum>
) {
	@Serializable
	data class Pensum(
		@SerialName("id") val id: String,
		@SerialName("career_code") val careerCode: Int,
		@SerialName("career_name") val careerName: String,
		@SerialName("year") val year: Int,
		@SerialName("modality_id") val modalityId: String,
		@SerialName("modality_name") val modalityName: String,
		@SerialName("total_credits") val totalCredits: Int,
		@SerialName("canvas") val canvas: Canvas,
		@SerialName("terms") val terms: List<Term>,
		@SerialName("nodes") val nodes: List<Node>,
		@SerialName("edges") val edges: List<Edge>
	)

	@Serializable
	data class Canvas(
		@SerialName("width") val width: Double,
		@SerialName("height") val height: Double
	)

	@Serializable
	data class Term(
		@SerialName("id") val id: String,
		@SerialName("label") val label: String,
		@SerialName("x") val x: Double,
		@SerialName("width") val width: Double
	)

	@Serializable
	data class Node(
		@SerialName("id") val id: String,
		@SerialName("node_type") val nodeType: String,
		@SerialName("display_code") val displayCode: String,
		@SerialName("subject_code") val subjectCode: String? = null,
		@SerialName("name") val name: String,
		@SerialName("credits") val credits: Int,
		@SerialName("category") val category: String,
		@SerialName("term_id") val termId: String,
		@SerialName("x") val x: Double,
		@SerialName("y") val y: Double,
		@SerialName("width") val width: Double,
		@SerialName("height") val height: Double,
		@SerialName("fulfillment_rules") val fulfillmentRules: List<FulfillmentRule> = emptyList()
	)

	@Serializable
	data class FulfillmentRule(
		@SerialName("id") val id: String,
		@SerialName("rule_type") val ruleType: String,
		@SerialName("subject_codes") val subjectCodes: List<String> = emptyList(),
		@SerialName("subject_code_prefixes") val subjectCodePrefixes: List<String> = emptyList(),
		@SerialName("min_credits") val minCredits: Int? = null,
		@SerialName("min_subjects") val minSubjects: Int? = null
	)

	@Serializable
	data class Edge(
		@SerialName("id") val id: String,
		@SerialName("from_node_id") val fromNodeId: String,
		@SerialName("to_node_id") val toNodeId: String,
		@SerialName("relationship_type") val relationshipType: String,
		@SerialName("points") val points: List<Point>
	)

	@Serializable
	data class Point(
		@SerialName("x") val x: Double,
		@SerialName("y") val y: Double
	)
}
