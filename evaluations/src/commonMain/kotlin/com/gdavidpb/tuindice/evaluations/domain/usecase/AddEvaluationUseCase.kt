package com.gdavidpb.tuindice.evaluations.domain.usecase

import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.evaluations.domain.mapper.toEvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.repository.EvaluationRepository
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.AddEvaluationUseCaseError
import com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler.AddEvaluationExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.validator.AddEvaluationParamsValidator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class AddEvaluationUseCase(
	private val evaluationRepository: EvaluationRepository,
	private val identifierRepository: IdentifierRepository,
	override val reportingRepository: ReportingRepository,
	override val paramsValidator: AddEvaluationParamsValidator,
	override val exceptionHandler: AddEvaluationExceptionHandler
) : FlowUseCase<AddEvaluationParams, Unit, AddEvaluationUseCaseError>(reportingRepository = reportingRepository) {
	override suspend fun executeOnBackground(params: AddEvaluationParams): Flow<Unit> {
		val evaluation = params.toEvaluationAdd(
			reference = identifierRepository.generateRandomIdentifier()
		)

		evaluationRepository.addEvaluation(
			add = evaluation
		)

		return flowOf(Unit)
	}
}
