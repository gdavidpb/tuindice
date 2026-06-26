package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.base.domain.usecase.base.InitialContentFreshness
import com.gdavidpb.tuindice.base.domain.usecase.base.InitialContentLoadResult
import com.gdavidpb.tuindice.base.domain.usecase.base.InitialContentRefreshPolicy
import com.gdavidpb.tuindice.base.domain.usecase.base.ensureInitialContentLoaded
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import kotlinx.coroutines.flow.Flow

class EnsureRecordLoadedUseCase(
	private val academicRecordRepository: AcademicRecordRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: RecordExceptionHandler
) : FlowUseCase<Unit, InitialContentLoadResult, RecordUseCaseError>() {
	override suspend fun executeOnBackground(params: Unit): Flow<InitialContentLoadResult> {
		return ensureInitialContentLoaded(
			hasLocalContent = {
				academicRecordRepository.getAcademicRecord()?.terms?.isNotEmpty() == true
			},
			refreshPolicy = InitialContentRefreshPolicy.Always,
			refresh = { request ->
				academicRecordRepository.updateAcademicRecord(
					forceRemote = request.freshness == InitialContentFreshness.ForceRemote
				)
			}
		)
	}
}
