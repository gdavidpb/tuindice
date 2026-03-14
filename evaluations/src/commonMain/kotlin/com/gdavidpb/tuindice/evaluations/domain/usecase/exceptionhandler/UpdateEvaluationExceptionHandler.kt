package com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.base.utils.extension.isNotFound
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.UpdateEvaluationUseCaseError

class UpdateEvaluationExceptionHandler(
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<UpdateEvaluationUseCaseError>() {
	override fun parseException(throwable: Throwable): UpdateEvaluationUseCaseError? {
		return when {
			throwable.isNotFound() -> UpdateEvaluationUseCaseError.NotFound
			else -> null
		}
	}
}
