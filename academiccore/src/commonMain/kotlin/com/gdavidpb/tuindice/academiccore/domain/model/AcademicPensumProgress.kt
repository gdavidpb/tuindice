package com.gdavidpb.tuindice.academiccore.domain.model

data class AcademicPensumProgress(
	val approvedCredits: Int,
	val nodeStatuses: Map<String, AcademicPensumNodeStatus>,
	val nodeFulfillments: Map<String, NodeFulfillment>
) {
	data class NodeFulfillment(
		val subjectCode: String,
		val subjectName: String
	)
}
