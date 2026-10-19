package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicAttempt
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.isSynthetic
import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantPlansRepository
import com.gdavidpb.tuindice.chatcontract.intent.CreatePlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.EditPlanArgs
import com.gdavidpb.tuindice.chatcontract.intent.RemovePlanArgs
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermPeriodOption
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.repository.AcademicRecordRepository
import com.gdavidpb.tuindice.record.domain.usecase.CreateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.DeleteSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.UpdateSyntheticTermUseCase
import com.gdavidpb.tuindice.record.domain.usecase.param.CreateSyntheticTermParams
import com.gdavidpb.tuindice.record.domain.usecase.param.DeleteSyntheticTermParams

/**
 * The plans of the assistant: the synthetic terms of the record. A plan is found by its term key, and its subjects come
 * from the pensum by their codes. A term that the record does not hold answers REJECTED with `unknown_term`, and a code
 * the pensum does not hold answers REJECTED with `unknown_subject`; neither mutates. The term is read from the
 * repository, which has no side effects: the use cases that look a term up either persist a selection or take an id.
 */
class RecordAssistantPlansDataSource(
	private val createSyntheticTermUseCase: CreateSyntheticTermUseCase,
	private val updateSyntheticTermUseCase: UpdateSyntheticTermUseCase,
	private val deleteSyntheticTermUseCase: DeleteSyntheticTermUseCase,
	private val recordIntentRunner: RecordIntentRunnerDataSource,
	private val academicRecordRepository: AcademicRecordRepository,
	private val pensumSubjectCatalog: PensumSubjectCatalogDataSource
) : AssistantPlansRepository {
	override suspend fun create(args: CreatePlanArgs, expectedRevision: Long?): IntentExecution {
		return guardIntent { createPlan(args, expectedRevision) }
	}

	override suspend fun edit(args: EditPlanArgs, expectedRevision: Long?): IntentExecution {
		return guardIntent { recordIntentRunner.revisionConflictOrNull(expectedRevision) ?: editTerm(args) }
	}

	override suspend fun remove(args: RemovePlanArgs, expectedRevision: Long?): IntentExecution {
		return guardIntent { recordIntentRunner.revisionConflictOrNull(expectedRevision) ?: removeTerm(args) }
	}

	private suspend fun createPlan(args: CreatePlanArgs, expectedRevision: Long?): IntentExecution {
		val periodCode = enumBySerialName<AcademicTermPeriod>(args.periodCode)
		val subjects = if (periodCode == null) null else resolveSubjects(args.subjectCodes)

		return when {
			periodCode == null -> invalidArgs()
			subjects == null -> unknownSubject()
			else -> recordIntentRunner.revisionConflictOrNull(expectedRevision)
				?: recordIntentRunner.runMutation(args.mutationId) {
					createSyntheticTermUseCase.execute(
						CreateSyntheticTermParams(
							editingTermId = null,
							editingTermKey = null,
							period = SyntheticTermPeriodOption(periodYear = args.periodYear, periodCode = periodCode),
							subjects = subjects,
							mutationId = args.mutationId
						)
					).settled()
				}
		}
	}

	private suspend fun editTerm(args: EditPlanArgs): IntentExecution {
		val term = syntheticTermByKey(args.termKey) ?: return unknownTerm()

		val codes = args.subjectCodes.map(::normalizeSubjectCode)
		val attemptByCode = term.attempts.associateBy { attempt -> normalizeSubjectCode(attempt.subjectCode) }
		val resolved = pensumSubjectCatalog.resolve(codes.filter { code -> code !in attemptByCode })

		return when {
			codes.any { code -> code !in attemptByCode && code !in resolved } -> unknownSubject()
			else -> {
				// A subject the term already holds keeps its attempt, its name and its credits. A new one takes the
				// pensum's.
				val subjects = codes.map { code ->
					attemptByCode[code]?.toSyntheticSubject(code) ?: resolved.getValue(code)
				}

				recordIntentRunner.runMutation(args.mutationId) {
					updateSyntheticTermUseCase.execute(
						CreateSyntheticTermParams(
							editingTermId = term.id,
							editingTermKey = term.termKey,
							period = term.period(),
							subjects = subjects,
							mutationId = args.mutationId
						)
					).settled()
				}
			}
		}
	}

	private suspend fun removeTerm(args: RemovePlanArgs): IntentExecution {
		val term = syntheticTermByKey(args.termKey) ?: return unknownTerm()

		return recordIntentRunner.runMutation(args.mutationId) {
			deleteSyntheticTermUseCase.execute(
				DeleteSyntheticTermParams(
					termId = term.id,
					mutationId = args.mutationId
				)
			).settled()
		}
	}

	// The subjects of the codes, from the pensum, in the order of the codes. Null when a code is not a course of it.
	private suspend fun resolveSubjects(subjectCodes: List<String>): List<SyntheticTermSubject>? {
		val resolved = pensumSubjectCatalog.resolve(subjectCodes)
		val codes = subjectCodes.map(::normalizeSubjectCode)

		return if (codes.all { code -> code in resolved }) codes.map { code -> resolved.getValue(code) } else null
	}

	// The synthetic term of the record that has this key, or null when there is none.
	private suspend fun syntheticTermByKey(termKey: String): AcademicTerm? {
		return academicRecordRepository.getAcademicRecord()
			?.terms
			?.firstOrNull { term -> term.kind.isSynthetic && term.termKey == termKey }
	}

	private fun AcademicTerm.period(): SyntheticTermPeriodOption {
		return SyntheticTermPeriodOption(periodYear = periodYear, periodCode = periodCode)
	}

	private fun AcademicAttempt.toSyntheticSubject(code: String): SyntheticTermSubject {
		return SyntheticTermSubject(
			attemptId = id,
			subjectCode = code,
			name = subjectName,
			credits = credits,
			gradingMode = gradingMode
		)
	}
}
