package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.record.data.repository.AcademicRecordRemoteDataRepository
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermLoadPreviewRepository

class SyntheticTermLoadPreviewDataSource(
	private val remoteDataRepository: AcademicRecordRemoteDataRepository
) : SyntheticTermLoadPreviewRepository {
	override suspend fun loadSyntheticTermPreview(subjectCodes: List<String>): SyntheticTermLoadPreview {
		return remoteDataRepository.loadSyntheticTermPreview(subjectCodes)
	}
}
