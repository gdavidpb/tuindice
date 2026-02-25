package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.mapper.toSubjectGradeSet
import com.gdavidpb.tuindice.record.domain.repository.QuarterRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.SubjectUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.SetSubjectGradeExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSubjectGradeParams
import com.gdavidpb.tuindice.record.domain.usecase.validator.SetSubjectGradeParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SetSubjectGradeUseCase(
	private val quarterRepository: QuarterRepository,
	override val paramsValidator: SetSubjectGradeParamsValidator,
	override val exceptionHandler: SetSubjectGradeExceptionHandler
) : FlowUseCase<SetSubjectGradeParams, Unit, SubjectUseCaseError>() {
	override suspend fun executeOnBackground(params: SetSubjectGradeParams): Flow<Unit> {
		val set = params.toSubjectGradeSet()

		quarterRepository.setSubjectGrade(set = set)

		return flowOf(Unit)
	}
}