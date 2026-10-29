package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import kotlinx.coroutines.flow.Flow

// Reads the local outcome flow only: nothing is sent and nothing is refreshed here.
class ObserveRecordMutationOutcomesUseCase(
	private val academicRecordRepository: AcademicRecordRepository,
	override val reportingRepository: ReportingRepository
) : FlowUseCase<Unit, MutationOutcome, Nothing>() {
	override suspend fun executeOnBackground(params: Unit): Flow<MutationOutcome> {
		return academicRecordRepository.observeMutationOutcomesFlow()
	}
}
