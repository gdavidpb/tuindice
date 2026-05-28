package com.gdavidpb.tuindice.pensum.domain.model

data class PensumProgress(
	val approvedCredits: Int,
	val nodeStatuses: Map<String, PensumNodeStatus>,
	val nodeFulfillments: Map<String, NodeFulfillment>
) {
	data class NodeFulfillment(
		val subjectCode: String,
		val subjectName: String
	)
}
