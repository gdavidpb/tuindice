package com.gdavidpb.tuindice.subjects.data.repository

import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult

interface SubjectStatsLocalDataRepository {
	suspend fun getSubjectDetail(subjectCode: String): SubjectDetailResult?
	suspend fun saveSubjectDetail(result: SubjectDetailResult)
}
