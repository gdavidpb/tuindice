package com.gdavidpb.tuindice.subjects.domain.repository

import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult

interface SubjectStatsRepository {
	suspend fun getFreshSubjectDetail(subjectCode: String): SubjectDetailResult?

	suspend fun refreshSubjectDetail(subjectCode: String): SubjectDetailResult
}
