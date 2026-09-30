package com.gdavidpb.tuindice.record.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CreateSyntheticTermPensumCacheResponse(
	@SerialName("selected_pensum_id") val selectedPensumId: String? = null,
	@SerialName("pensums") val pensums: List<Pensum> = emptyList(),
	@SerialName("pensum") val legacyPensum: Pensum? = null
) {
	val pensum: Pensum
		get() = legacyPensum
			?: pensums.firstOrNull { pensum -> pensum.id == selectedPensumId }
			?: pensums.firstOrNull()
			?: Pensum()

	@Serializable
	internal data class Pensum(
		@SerialName("id") val id: String? = null,
		@SerialName("nodes") val nodes: List<Node> = emptyList(),
		@SerialName("edges") val edges: List<Edge> = emptyList()
	)

	@Serializable
	internal data class Node(
		@SerialName("id") val id: String,
		@SerialName("node_type") val nodeType: String,
		@SerialName("subject_code") val subjectCode: String? = null,
		@SerialName("name") val name: String,
		@SerialName("credits") val credits: Int,
		// Both default: a cache written before rules were read (or by the legacy `pensum` shape)
		// must still decode, it just resolves no slot or equivalence.
		@SerialName("category") val category: String? = null,
		@SerialName("fulfillment_rules") val fulfillmentRules: List<FulfillmentRule> = emptyList()
	)

	@Serializable
	internal data class FulfillmentRule(
		@SerialName("rule_type") val ruleType: String,
		@SerialName("subject_codes") val subjectCodes: List<String> = emptyList(),
		@SerialName("subject_code_prefixes") val subjectCodePrefixes: List<String> = emptyList(),
		@SerialName("slot_eligibility_kind") val slotEligibilityKind: String? = null,
		@SerialName("min_credits") val minCredits: Int? = null,
		@SerialName("min_subjects") val minSubjects: Int? = null
	)

	@Serializable
	internal data class Edge(
		@SerialName("from_node_id") val fromNodeId: String,
		@SerialName("to_node_id") val toNodeId: String,
		@SerialName("relationship_type") val relationshipType: String
	)
}
