package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.exception.SyntheticTermValidationException
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermUpdateCommand
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermValidationError
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.CreateSyntheticTermParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpdateSyntheticTermUseCase(
	private val repository: AcademicRecordRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: RecordExceptionHandler
) : FlowUseCase<CreateSyntheticTermParams, Unit, RecordUseCaseError>(
	reportingRepository = reportingRepository
) {
	override suspend fun executeOnBackground(params: CreateSyntheticTermParams): Flow<Unit> {
		val targetTermId = requireNotNull(params.editingTermId)
		val targetTermKey = requireNotNull(params.editingTermKey)
		val record = repository.getAcademicRecord()
			?: throw SyntheticTermValidationException(SyntheticTermValidationError.RECORD_UNAVAILABLE)
		SyntheticTermCommandValidator.validate(
			record = record,
			params = params
		)

		val keepsTermIdentity = params.period.termKey == targetTermKey
		val termId = if (keepsTermIdentity) targetTermId else params.period.termKey

		repository.updateSyntheticTerm(
			SyntheticTermUpdateCommand(
				targetTermId = targetTermId,
				targetTermKey = targetTermKey,
				termId = termId,
				periodYear = params.period.periodYear,
				periodCode = params.period.periodCode,
				attempts = params.subjects.map { subject ->
					SyntheticTermUpdateCommand.SyntheticAttemptSeed(
						attemptId = if (keepsTermIdentity) {
							subject.attemptId ?: "$termId-${subject.subjectCode}"
						} else {
							"$termId-${subject.subjectCode}"
						},
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
