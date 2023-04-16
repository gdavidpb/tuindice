package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.mapper.toSubjectUpdate
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.UpdateSubjectExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.UpdateSubjectParams
import com.gdavidpb.tuindice.record.domain.usecase.validator.UpdateSubjectParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateSubjectUseCase(
	private val authRepository: AuthRepository,
	private val quarterRepository: QuarterRepository,
	override val paramsValidator: UpdateSubjectParamsValidator,
	override val exceptionHandler: UpdateSubjectExceptionHandler
) : FlowUseCase<UpdateSubjectParams, Unit, SubjectError>() {
	override suspend fun executeOnBackground(params: UpdateSubjectParams): Flow<Unit> {
		val activeUId = authRepository.getActiveAuth().uid

		val update = params.toSubjectUpdate()

		quarterRepository.updateSubject(
			uid = activeUId,
			update = update
		)

		return flowOf(Unit)
	}
}