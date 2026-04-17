package com.gdavidpb.tuindice.subjects.domain.repository

import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult

interface SubjectStatsRepository {
	suspend fun getSubjectDetail(
		subjectCode: String,
		forceRefresh: Boolean = false
	): SubjectDetailResult
}
