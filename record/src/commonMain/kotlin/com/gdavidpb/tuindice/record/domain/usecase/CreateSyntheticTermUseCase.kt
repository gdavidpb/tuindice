package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.exception.SyntheticTermValidationException
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermCreationCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermValidationError
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.CreateSyntheticTermParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class CreateSyntheticTermUseCase(
	private val repository: AcademicRecordRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: RecordExceptionHandler
) : FlowUseCase<CreateSyntheticTermParams, Unit, RecordUseCaseError>(
	reportingRepository = reportingRepository
) {
	override suspend fun executeOnBackground(params: CreateSyntheticTermParams): Flow<Unit> {
		val record = repository.getAcademicRecord()
			?: throw SyntheticTermValidationException(SyntheticTermValidationError.RECORD_UNAVAILABLE)
		SyntheticTermCommandValidator.validate(
			record = record,
			params = params
		)

		val termId = params.period.termKey
		repository.addSyntheticTerm(
			SyntheticTermCreationCommand(
				termId = termId,
				periodYear = params.period.periodYear,
				periodCode = params.period.periodCode,
				attempts = params.subjects.map { subject ->
					SyntheticTermCreationCommand.SyntheticAttemptSeed(
						attemptId = "$termId-${subject.subjectCode}",
						subjectCode = subject.subjectCode,
						subjectName = subject.name,
						credits = subject.credits,
						gradingMode = subject.gradingMode,
						score = AttemptScore.empty(),
						outcome = AttemptOutcome.PENDING
					)
				}
			)
		)
		return flowOf(Unit)
	}
}
