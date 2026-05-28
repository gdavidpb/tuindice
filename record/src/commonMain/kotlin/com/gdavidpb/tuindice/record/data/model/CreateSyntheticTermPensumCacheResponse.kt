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
		@SerialName("credits") val credits: Int
	)

	@Serializable
	internal data class Edge(
		@SerialName("from_node_id") val fromNodeId: String,
		@SerialName("to_node_id") val toNodeId: String,
		@SerialName("relationship_type") val relationshipType: String
	)
}
