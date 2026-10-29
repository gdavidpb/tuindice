package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// The key of the term whose id is given, as the local record knows it; null when the record has no such term. Nothing
// is sent and nothing is refreshed here.
class GetTermKeyUseCase(
	private val academicRecordRepository: AcademicRecordRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<String, String?, Nothing>() {
	override suspend fun executeOnBackground(params: String): Flow<String?> {
		val term = academicRecordRepository.getAcademicRecord()
			?.terms
			?.firstOrNull { term -> term.id == params }

		return flowOf(term?.termKey)
	}
}
