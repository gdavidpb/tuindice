package com.gdavidpb.tuindice.record.domain.usecase

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.isSynthetic
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.FlowUseCase
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermEditSeed
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.usecase.error.RecordUseCaseError
import com.gdavidpb.tuindice.record.domain.usecase.exceptionhandler.RecordExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class LoadSyntheticTermEditSeedUseCase(
	private val repository: AcademicRecordRepository,
	override val reportingRepository: ReportingRepository,
	override val exceptionHandler: RecordExceptionHandler
) : FlowUseCase<String, SyntheticTermEditSeed, RecordUseCaseError>() {
	override suspend fun executeOnBackground(params: String): Flow<SyntheticTermEditSeed> {
		val record = requireNotNull(repository.getAcademicRecord())
		val term = record.terms.first { term -> term.id == params && term.kind.isSynthetic }

		return flowOf(term.toEditSeed())
	}

	private fun AcademicTerm.toEditSeed(): SyntheticTermEditSeed {
		return SyntheticTermEditSeed(
			termId = id,
			termKey = termKey,
			period = SyntheticTermPeriodOption(
				periodYear = periodYear,
				periodCode = periodCode
			),
			subjects = attempts.map { attempt ->
				SyntheticTermSubject(
					attemptId = attempt.id,
					subjectCode = attempt.subjectCode,
					name = attempt.subjectName,
					credits = attempt.credits,
					gradingMode = attempt.gradingMode
				)
			}
		)
	}
}
