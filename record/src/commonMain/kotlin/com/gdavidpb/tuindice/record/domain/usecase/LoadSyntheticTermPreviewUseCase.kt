package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermLoadPreview
import com.gdavidpb.tuindice.record.domain.repository.SyntheticTermLoadPreviewRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.LoadSyntheticTermPreviewParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class LoadSyntheticTermPreviewUseCase(
	private val repository: SyntheticTermLoadPreviewRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: RecordExceptionHandler
) : FlowUseCase<LoadSyntheticTermPreviewParams, SyntheticTermLoadPreview, RecordUseCaseError>(
	reportingRepository = reportingRepository
) {
	override suspend fun executeOnBackground(params: LoadSyntheticTermPreviewParams): Flow<SyntheticTermLoadPreview> {
		return flowOf(repository.loadSyntheticTermPreview(params.subjectCodes))
	}
}
