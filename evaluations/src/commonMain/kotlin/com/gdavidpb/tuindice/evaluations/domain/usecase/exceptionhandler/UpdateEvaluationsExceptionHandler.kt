package com.gdavidpb.tuindice.evaluations.domain.usecase.exceptionhandler

import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.ExceptionHandler
import com.gdavidpb.tuindice.evaluations.domain.usecase.error.UpdateEvaluationsUseCaseError

class UpdateEvaluationsExceptionHandler(
	override val reportingRepository: ReportingRepository
) : ExceptionHandler<UpdateEvaluationsUseCaseError>()
