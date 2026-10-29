package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.DeleteSyntheticTermParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class DeleteSyntheticTermUseCase(
	private val academicRecordRepository: AcademicRecordRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: RecordExceptionHandler
) : FlowUseCase<DeleteSyntheticTermParams, Unit, RecordUseCaseError>() {
	override suspend fun executeOnBackground(params: DeleteSyntheticTermParams): Flow<Unit> {
		academicRecordRepository.deleteSyntheticTerm(
			termId = params.termId,
			mutationId = params.mutationId
		)
		return flowOf(Unit)
	}
}
