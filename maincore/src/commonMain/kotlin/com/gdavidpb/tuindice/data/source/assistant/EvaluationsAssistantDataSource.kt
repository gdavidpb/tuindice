package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.academiccore.domain.model.EvaluationType
import com.gdavidpb.tuindice.assistant.domain.model.IntentExecution
import com.gdavidpb.tuindice.assistant.domain.repository.AssistantEvaluationsRepository
import com.gdavidpb.tuindice.base.domain.model.mutation.MutationOutcome
import com.gdavidpb.tuindice.chatcontract.intent.AddEvaluationsArgs
import com.gdavidpb.tuindice.chatcontract.intent.EditEvaluationArgs
import com.gdavidpb.tuindice.chatcontract.intent.EvaluationDraft
import com.gdavidpb.tuindice.chatcontract.intent.RemoveEvaluationArgs
import com.gdavidpb.tuindice.evaluations.domain.usecase.AddEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.ObserveEvaluationMutationOutcomesUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.RemoveEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.UpdateEvaluationUseCase
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.AddEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.RemoveEvaluationParams
import com.gdavidpb.tuindice.evaluations.domain.usecase.param.UpdateEvaluationParams
import kotlinx.coroutines.flow.Flow

/**
 * Runs the evaluation intents of the assistant through the evaluations use cases, each mutation under the id its args
 * carry, and answers how each one ended. The `reference` of an evaluation draft is not sent: the app generates its own
 * reference for every evaluation it adds, so the draft's one does not reach the mutation.
 */
class EvaluationsAssistantDataSource(
	private val addEvaluationUseCase: AddEvaluationUseCase,
	private val updateEvaluationUseCase: UpdateEvaluationUseCase,
	private val removeEvaluationUseCase: RemoveEvaluationUseCase,
	private val observeEvaluationMutationOutcomesUseCase: ObserveEvaluationMutationOutcomesUseCase
) : AssistantEvaluationsRepository {
	override suspend fun add(args: AddEvaluationsArgs): IntentExecution {
		return guardIntent { addEvaluations(args) }
	}

	override suspend fun edit(args: EditEvaluationArgs): IntentExecution {
		return guardIntent { editEvaluation(args) }
	}

	override suspend fun remove(args: RemoveEvaluationArgs): IntentExecution {
		return guardIntent { removeEvaluation(args) }
	}

	// One mutation per selected item. Every item is read before any runs: one name the app does not know refuses the
	// whole batch, with nothing mutated.
	private suspend fun addEvaluations(args: AddEvaluationsArgs): IntentExecution {
		val selected = args.items.filter { item -> item.selected }
		val params = selected.mapNotNull(::addParams)
		if (selected.isEmpty() || params.size != selected.size) return invalidArgs()

		val mutations = selected.zip(params) { item, param ->
			AssistantMutation(item.mutationId) { addEvaluationUseCase.execute(param).settled() }
		}

		return settleMutations(mutations, observeMutationOutcomes())
	}

	private suspend fun editEvaluation(args: EditEvaluationArgs): IntentExecution {
		val type = args.evaluationType?.let { name -> enumByName<EvaluationType>(name) }
		val scheduleMode = args.scheduleMode?.let { serialName ->
			enumBySerialName<EvaluationScheduleMode>(serialName)
		}

		// A name the app does not know refuses the edit. An absent one keeps the field as it is.
		val unknownName = (args.evaluationType != null && type == null) ||
				(args.scheduleMode != null && scheduleMode == null)
		if (unknownName) return invalidArgs()

		val params = UpdateEvaluationParams(
			evaluationId = args.evaluationId,
			attemptId = null,
			subjectCode = null,
			termId = null,
			scheduleMode = scheduleMode,
			grade = args.grade,
			maxGrade = args.maxGrade,
			date = args.dateMillis,
			type = type,
			mutationId = args.mutationId
		)
		val mutation = AssistantMutation(args.mutationId) { updateEvaluationUseCase.execute(params).settled() }

		return settleMutations(listOf(mutation), observeMutationOutcomes())
	}

	private suspend fun removeEvaluation(args: RemoveEvaluationArgs): IntentExecution {
		val params = RemoveEvaluationParams(
			evaluationId = args.evaluationId,
			mutationId = args.mutationId
		)
		val mutation = AssistantMutation(args.mutationId) { removeEvaluationUseCase.execute(params).settled() }

		return settleMutations(listOf(mutation), observeMutationOutcomes())
	}

	// Null when the draft names a type or a schedule mode the app does not know.
	private fun addParams(item: EvaluationDraft): AddEvaluationParams? {
		val type = enumByName<EvaluationType>(item.evaluationType)
		val scheduleMode = enumBySerialName<EvaluationScheduleMode>(item.scheduleMode)
		if (type == null || scheduleMode == null) return null

		return AddEvaluationParams(
			attemptId = item.attemptId,
			subjectCode = item.subjectCode,
			termId = item.termId,
			type = type,
			scheduleMode = scheduleMode,
			date = item.dateMillis,
			grade = item.grade,
			maxGrade = item.maxGrade,
			mutationId = item.mutationId
		)
	}

	private fun observeMutationOutcomes(): Flow<MutationOutcome> {
		return observeEvaluationMutationOutcomesUseCase.execute(Unit).outcomes()
	}
}
