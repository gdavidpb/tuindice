package com.gdavidpb.tuindice.evaluations.data.resolver

import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluation
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.mutation.EvaluationMutation
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope

class VisibleEvaluationsStateResolver {
	fun resolveVisibleState(
		confirmedSnapshot: LocalEvaluationsSnapshot,
		pendingMutations: List<MutationEnvelope<String, EvaluationMutation>>
	): List<LocalEvaluation> {
		var visibleEvaluations = confirmedSnapshot.evaluations

		pendingMutations.forEach { mutation ->
			when (val command = mutation.command) {
				is EvaluationMutation.Add -> {
					if (visibleEvaluations.none { evaluation ->
							evaluation.referenceId == command.referenceId || evaluation.id == command.referenceId
						}
					) {
						visibleEvaluations = visibleEvaluations + LocalEvaluation(
							id = command.referenceId,
							referenceId = command.referenceId,
							subjectId = command.subjectId,
							subjectCode = command.subjectCode,
							quarterId = command.quarterId,
							revision = 0L,
							scheduleMode = command.scheduleMode,
							grade = command.grade,
							maxGrade = command.maxGrade,
							date = command.date,
							type = command.type,
							isDone = command.grade != null
						)
					}
				}

				is EvaluationMutation.Update -> {
					visibleEvaluations = visibleEvaluations.map { evaluation ->
						if (evaluation.id == command.evaluationId) {
							evaluation.apply(command)
						} else {
							evaluation
						}
					}
				}

				is EvaluationMutation.Remove -> {
					visibleEvaluations = visibleEvaluations.filterNot { evaluation ->
						evaluation.id == command.evaluationId
					}
				}
			}
		}

		return visibleEvaluations.sortedBy { evaluation -> evaluation.date ?: Long.MAX_VALUE }
	}

	private fun LocalEvaluation.apply(
		command: EvaluationMutation.Update
	): LocalEvaluation {
		val resolvedScheduleMode = command.scheduleMode ?: if (command.date != null) {
			EvaluationScheduleMode.DATED
		} else {
			scheduleMode
		}
		val resolvedDate = when (resolvedScheduleMode) {
			EvaluationScheduleMode.CONTINUOUS -> null
			EvaluationScheduleMode.DATED -> command.date ?: date
		}
		val resolvedGrade = command.grade

		return copy(
			scheduleMode = resolvedScheduleMode,
			grade = resolvedGrade,
			maxGrade = command.maxGrade ?: maxGrade,
			date = resolvedDate,
			type = command.type ?: type,
			isDone = resolvedGrade != null
		)
	}
}
