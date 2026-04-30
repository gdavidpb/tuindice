package com.gdavidpb.tuindice.subjects.data.source

import com.gdavidpb.tuindice.subjects.data.repository.SubjectStatsLocalDataRepository
import com.gdavidpb.tuindice.subjects.domain.model.SubjectDetailResult

class DebugSubjectStatsLocalDataSource(
	private val localDataSource: SubjectStatsLocalDataRepository
) : SubjectStatsLocalDataRepository {
	override suspend fun getSubjectDetail(subjectCode: String): SubjectDetailResult? {
		if (DebugSubjectRemoteMockCodes.matches(subjectCode)) return null

		return localDataSource.getSubjectDetail(subjectCode)
	}

	override suspend fun saveSubjectDetail(result: SubjectDetailResult) {
		localDataSource.saveSubjectDetail(result)
	}
}
