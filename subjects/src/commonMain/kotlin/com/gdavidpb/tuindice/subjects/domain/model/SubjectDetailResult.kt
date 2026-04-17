package com.gdavidpb.tuindice.subjects.domain.model

sealed interface SubjectDetailResult {
	data class Ready(
		val detail: SubjectDetail
	) : SubjectDetailResult

	data class Unavailable(
		val subjectCode: String,
		val expiresAt: Long
	) : SubjectDetailResult
}
