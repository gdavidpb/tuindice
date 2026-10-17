package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// Reads the local revision only: nothing is sent and nothing is refreshed here.
class GetRecordRevisionUseCase(
	private val academicRecordRepository: AcademicRecordRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, Long?, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<Long?> {
		return flowOf(academicRecordRepository.getRecordRevision())
	}
}
