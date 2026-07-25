package com.gdavidpb.tuindice.record.presentation.transition

import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOverride
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.utils.currentTimeMillis
import com.gdavidpb.tuindice.record.domain.mapper.attemptSelectionToOverridePayload
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.machine.RecordInternalEvent
import com.gdavidpb.tuindice.record.presentation.machine.RecordMachine

internal fun MachineDefinitionBuilder<Record.State>.recordContentTransitions(
	machine: RecordMachine,
	host: MachineHost<Record.Effect>
) {
	from<Record.State.Content> {
		// Selecting a term only makes sense with content on screen: the row reads the
		// view mode from S instead of trusting a payload copy, and any other state
		// rejects the action with telemetry.
		on<Record.Action.SelectTerm> { state, action ->
			machine.selectTerm(
				host = host,
				termId = action.termId,
				viewMode = state.viewMode
			)
			state
		}

		// Arrastrar el slider no toca la capa de datos: el override vive en S mientras
		// dura el gesto, y la pantalla proyecta `visibleRecord`. Solo al soltar
		// (`commit`) se encola la mutación.
		on<Record.Action.UpsertAttemptSelection> { state, action ->
			if (action.commit) {
				machine.upsertAttemptSelection(
					host = host,
					attemptId = action.attemptId,
					grade = action.grade,
					outcome = action.outcome
				)
			}

			// `AttemptScore.numeric` valida el rango y lanza. Una excepción aquí mata el
			// loop de la máquina, así que un payload inválido deja el estado intacto y
			// la validación real queda donde ya estaba: en el use case.
			val payload = runCatching {
				attemptSelectionToOverridePayload(
					grade = action.grade,
					outcome = action.outcome
				)
			}.getOrNull() ?: return@on state

			state.copy(
				inFlightSelection = Record.State.InFlightSelection(
					override = AttemptOverride(
						attemptId = action.attemptId,
						score = payload.first,
						outcome = payload.second,
						updatedAtMillis = currentTimeMillis()
					),
					isCommitted = action.commit
				)
			)
		}

		on<RecordInternalEvent.RecordObservationFailed> { state, _ -> state }

		on<RecordInternalEvent.RecordRefreshStarted> { state, _ -> state }

		// A failed refresh keeps the on-screen record; the user still gets told,
		// mirroring the Summary refresh-failure feedback.
		on<RecordInternalEvent.RecordRefreshFailed>(
			emits = setOf(
				Record.Effect.NavigateToOutdatedCredentials::class,
				Record.Effect.ShowSnackBar::class
			)
		) { state, event ->
			if (event.navigateToOutdatedCredentials) {
				host.sendEffect(Record.Effect.NavigateToOutdatedCredentials)
			} else {
				host.sendEffect(Record.Effect.ShowSnackBar(message = event.message))
			}

			state
		}
	}
}
