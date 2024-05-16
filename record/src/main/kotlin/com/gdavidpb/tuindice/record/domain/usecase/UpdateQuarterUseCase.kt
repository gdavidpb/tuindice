package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.mapper.toQuarterUpdate
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.UpdateSubjectExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.UpdateQuarterParams
import com.gdavidpb.tuindice.record.domain.usecase.validator.UpdateQuarterParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateQuarterUseCase(
	private val authRepository: AuthRepository,
	private val quarterRepository: QuarterRepository,
	override val paramsValidator: UpdateQuarterParamsValidator,
	override val exceptionHandler: UpdateSubjectExceptionHandler
) : FlowUseCase<UpdateQuarterParams, Unit, SubjectError>() {
	override suspend fun executeOnBackground(params: UpdateQuarterParams): Flow<Unit> {
		val activeUId = authRepository.getActiveAuth().uid

		val update = params.toQuarterUpdate()

		quarterRepository.updateQuarter(
			uid = activeUId,
			update = update
		)

		return flowOf(Unit)
	}
}