package com.gdavidpb.tuindice.subjects.data.repository

import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult

interface SubjectStatsApiDataRepository {
	suspend fun getSubjectDetail(subjectCode: String): SubjectDetailResult
}
