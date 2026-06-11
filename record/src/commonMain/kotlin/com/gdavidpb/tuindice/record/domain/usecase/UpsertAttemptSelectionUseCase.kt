package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.mapper.attemptSelectionToOverridePayload
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import com.gdavidpb.tuindice.record.domain.usecase.param.UpsertAttemptSelectionParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class UpsertAttemptSelectionUseCase(
	private val academicRecordRepository: AcademicRecordRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: RecordExceptionHandler,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : FlowUseCase<UpsertAttemptSelectionParams, Unit, RecordUseCaseError>(
	reportingRepository = reportingRepository,
	dispatchers = dispatchers
) {
	override suspend fun executeOnBackground(params: UpsertAttemptSelectionParams): Flow<Unit> {
		val (score, outcome) = attemptSelectionToOverridePayload(
			grade = params.grade,
			outcome = params.outcome
		)

		if (params.commit && shouldClearOverride(
				attemptId = params.attemptId,
				score = score,
				outcome = outcome
			)
		) {
			academicRecordRepository.deleteAttemptOverride(params.attemptId)
		} else {
			academicRecordRepository.upsertAttemptOverride(
				attemptId = params.attemptId,
				score = score,
				outcome = outcome,
				commit = params.commit
			)
		}

		return flowOf(Unit)
	}

	private suspend fun shouldClearOverride(
		attemptId: String,
		score: AttemptScore?,
		outcome: AttemptOutcome?
	): Boolean {
		val academicAttempt = academicRecordRepository.getAcademicRecord()
			?.terms
			?.flatMap { term -> term.attempts }
			?.firstOrNull { attempt -> attempt.id == attemptId }
			?: return false

		return academicAttempt.academicScore == score &&
			academicAttempt.academicOutcome == (outcome ?: academicAttempt.academicOutcome)
	}
}
