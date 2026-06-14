package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermCreationRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.RefreshSyntheticTermSubjectSearchParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class RefreshSyntheticTermSubjectSearchUseCase(
	private val repository: SyntheticTermCreationRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: RecordExceptionHandler
) : FlowUseCase<RefreshSyntheticTermSubjectSearchParams, Unit, RecordUseCaseError>() {
	override suspend fun executeOnBackground(params: RefreshSyntheticTermSubjectSearchParams): Flow<Unit> {
		repository.refreshSearch(params.query)
		return flowOf(Unit)
	}
}
