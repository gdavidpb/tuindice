package com.gdavidpb.tuindice.record.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CreateSyntheticTermPensumCacheResponse(
	@SerialName("pensum") val pensum: Pensum
) {
	@Serializable
	internal data class Pensum(
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
