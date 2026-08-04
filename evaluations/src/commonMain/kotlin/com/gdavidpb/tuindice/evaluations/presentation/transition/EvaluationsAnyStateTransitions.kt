package com.gdavidpb.tuindice.evaluations.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.evaluations.presentation.contract.Evaluations
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsInternalEvent
import com.gdavidpb.tuindice.evaluations.presentation.machine.EvaluationsMachine

internal fun MachineDefinitionBuilder<Evaluations.State>.evaluationsAnyStateTransitions(
	machine: EvaluationsMachine,
	host: MachineHost<Evaluations.Effect>
) {
	fromAny {
		// Observation may (re)start from any state: the initial action races the
		// route's startup refresh, and the retry button re-dispatches it from Failed.
		on<Evaluations.Action.LoadEvaluations> { state, _ ->
			machine.startObservation(host = host)
			state
		}

		on<Evaluations.Action.RefreshEvaluations> { state, _ ->
			machine.refresh(host = host)
			state
		}

		on<Evaluations.Action.EnsureEvaluationsLoaded> { state, _ ->
			machine.ensureLoaded(host = host)
			state
		}

		on<Evaluations.Action.AddEvaluation>(
			emits = setOf(Evaluations.Effect.NavigateToAddEvaluation::class)
		) { state, _ ->
			host.sendEffect(Evaluations.Effect.NavigateToAddEvaluation)
			state
		}

		on<Evaluations.Action.EditEvaluation>(
			emits = setOf(Evaluations.Effect.NavigateToEvaluation::class)
		) { state, action ->
			host.sendEffect(
				Evaluations.Effect.NavigateToEvaluation(
					evaluationId = action.evaluationId
				)
			)
			state
		}

		on<Evaluations.Action.ShowEvaluationGradeDialog> { state, action ->
			machine.loadGradePicker(host = host, action = action)
			state
		}

		on<Evaluations.Action.SetEvaluationGrade> { state, action ->
			machine.saveGrade(host = host, action = action)
			state
		}

		on<Evaluations.Action.RemoveEvaluation> { state, action ->
			machine.remove(host = host, evaluationId = action.evaluationId)
			state
		}

		onTo<EvaluationsInternalEvent.EvaluationsWaitingObserved, Evaluations.State.Loading> { _, _ ->
			Evaluations.State.Loading
		}

		onTo<
			EvaluationsInternalEvent.EvaluationsRecordDataUnavailableObserved,
			Evaluations.State.RecordDataUnavailable,
			> { _, _ ->
			Evaluations.State.RecordDataUnavailable
		}

		onTo<EvaluationsInternalEvent.EvaluationsNoAttemptsObserved, Evaluations.State.NoAttempts> { _, event ->
			Evaluations.State.NoAttempts(reason = event.reason)
		}

		onTo<EvaluationsInternalEvent.EvaluationsContentObserved, Evaluations.State.Content> { state, event ->
			state.toObservedContent(event)
		}

		onTo<EvaluationsInternalEvent.EvaluationsEmptyObserved, Evaluations.State.Empty> { _, _ ->
			Evaluations.State.Empty
		}

		onTo<EvaluationsInternalEvent.EvaluationsEmptyConfirmed, Evaluations.State.Empty> { _, _ ->
			Evaluations.State.Empty
		}

		onTo<EvaluationsInternalEvent.EvaluationsObservationFailed, Evaluations.State.Failed> { _, event ->
			Evaluations.State.Failed(message = event.message)
		}

		onTo<EvaluationsInternalEvent.EvaluationsRefreshStarted, Evaluations.State.Loading> { _, _ ->
			Evaluations.State.Loading
		}

		onTo<EvaluationsInternalEvent.EvaluationsRefreshFailed, Evaluations.State.Failed> { _, event ->
			Evaluations.State.Failed(message = event.message)
		}

		on<EvaluationsInternalEvent.GradePickerLoaded>(
			emits = setOf(Evaluations.Effect.NavigateToGradePickerDialog::class)
		) { state, event ->
			host.sendEffect(
				Evaluations.Effect.NavigateToGradePickerDialog(
					evaluationId = event.evaluationId,
					evaluationName = event.evaluationName,
					subjectCode = event.subjectCode,
					grade = event.grade,
					maxGrade = event.maxGrade
				)
			)
			state
		}

		on<EvaluationsInternalEvent.GradePickerLoadFailed>(
			emits = setOf(Evaluations.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Evaluations.Effect.ShowSnackBar(message = event.message))
			state
		}

		on<EvaluationsInternalEvent.EvaluationGradeSaved>(
			emits = setOf(Evaluations.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Evaluations.Effect.ShowSnackBar(message = event.message))
			state
		}

		on<EvaluationsInternalEvent.EvaluationGradeSaveFailed>(
			emits = setOf(Evaluations.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Evaluations.Effect.ShowSnackBar(message = event.message))
			state
		}

		on<EvaluationsInternalEvent.EvaluationRemoved>(
			emits = setOf(Evaluations.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Evaluations.Effect.ShowSnackBar(message = event.message))
			state
		}

		on<EvaluationsInternalEvent.EvaluationRemoveFailed>(
			emits = setOf(Evaluations.Effect.ShowSnackBar::class)
		) { state, event ->
			host.sendEffect(Evaluations.Effect.ShowSnackBar(message = event.message))
			state
		}
	}
}

// Selected-week preservation across re-emissions, exactly as the old reducer: keep the
// current selection when it still exists, fall back to the default key, then to the
// first available week.
private fun Evaluations.State.toObservedContent(
	event: EvaluationsInternalEvent.EvaluationsContentObserved
): Evaluations.State.Content {
	val selectedWeekKey = when (this) {
		is Evaluations.State.Content -> selectedWeekKey
		else -> event.defaultWeekKey
	}.takeIf { key ->
		event.weekItems.any { item -> item.key == key }
	} ?: event.defaultWeekKey.takeIf { key ->
		event.weekItems.any { item -> item.key == key }
	} ?: event.weekItems.first().key

	return Evaluations.State.Content(
		weekItem = event.weekItems.first { item -> item.key == selectedWeekKey },
		weekItems = event.weekItems,
		selectedWeekKey = selectedWeekKey,
		evaluationGroups = event.evaluationWeekGroups.flatMap { weekGroup -> weekGroup.groups },
		evaluationWeekGroups = event.evaluationWeekGroups
	)
}
