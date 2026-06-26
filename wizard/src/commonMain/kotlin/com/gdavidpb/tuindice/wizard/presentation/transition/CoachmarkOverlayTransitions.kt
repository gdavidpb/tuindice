package com.gdavidpb.tuindice.wizard.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.wizard.presentation.contract.CoachmarkOverlay
import com.gdavidpb.tuindice.wizard.presentation.machine.CoachmarkOverlayInternalEvent
import com.gdavidpb.tuindice.wizard.presentation.machine.CoachmarkOverlayMachine
import com.gdavidpb.tuindice.wizard.presentation.model.Coachmark
import com.gdavidpb.tuindice.wizard.presentation.model.persistedId

internal fun MachineDefinitionBuilder<CoachmarkOverlay.State>.coachmarkOverlayTransitions(
	machine: CoachmarkOverlayMachine,
	host: MachineHost<CoachmarkOverlay.Effect>
) {
	from<CoachmarkOverlay.State> {
		on<CoachmarkOverlay.Action.SurfaceChanged> { state, action ->
			val visitKey = action.surface.visitKey

			when {
				visitKey in state.presentedVisitKeys ->
					state.withoutCoachmarks(visitKey = visitKey)

				action.surface.eligibleCoachmarkIds.isEmpty() ->
					state.withoutCoachmarks(visitKey = visitKey)

				state.activeCoachmark != null && state.currentVisitKey == visitKey ->
					state

				else -> {
					machine.resolveCoachmark(host = host, surface = action.surface)
					state.withoutCoachmarks(visitKey = visitKey)
				}
			}
		}

		on<CoachmarkOverlay.Action.PrimaryActionClick> { state, _ ->
			val activeCoachmark = state.activeCoachmark ?: return@on state
			val visitKey = state.currentVisitKey ?: return@on state.withoutCoachmarks()

			machine.markCoachmarkSeen(
				host = host,
				coachmarkId = activeCoachmark.id.persistedId
			)

			state.advanceAfterPrimaryActionClick(visitKey = visitKey)
		}

		on<CoachmarkOverlay.Action.PreviousActionClick> { state, _ ->
			state.moveToPreviousCoachmark()
		}

		on<CoachmarkOverlayInternalEvent.CoachmarkResolved> { state, event ->
			if (
				state.currentVisitKey != event.visitKey ||
				event.visitKey in state.presentedVisitKeys
			) {
				return@on state
			}

			val resolution = event.resolution ?: return@on state.withoutCoachmarks()
			val pendingCoachmarks = resolution.pendingCoachmarkIds
				.mapNotNull { coachmarkId -> machine.coachmarkFor(coachmarkId) }

			state.withPendingCoachmarks(pendingCoachmarks = pendingCoachmarks)
		}
	}
}

private fun CoachmarkOverlay.State.withoutCoachmarks(
	visitKey: String? = currentVisitKey
): CoachmarkOverlay.State {
	return copy(
		activeCoachmark = null,
		previousCoachmarks = emptyList(),
		pendingCoachmarks = emptyList(),
		currentVisitKey = visitKey
	)
}

private fun CoachmarkOverlay.State.advanceAfterPrimaryActionClick(
	visitKey: String
): CoachmarkOverlay.State {
	val activeCoachmark = activeCoachmark ?: return this
	val remainingCoachmarks = pendingCoachmarks.drop(1)

	return if (remainingCoachmarks.isNotEmpty()) {
		withPendingCoachmarks(
			previousCoachmarks = previousCoachmarks + activeCoachmark,
			pendingCoachmarks = remainingCoachmarks
		)
	} else {
		withoutCoachmarks().copy(
			presentedVisitKeys = presentedVisitKeys + visitKey
		)
	}
}

private fun CoachmarkOverlay.State.withPendingCoachmarks(
	previousCoachmarks: List<Coachmark> = emptyList(),
	pendingCoachmarks: List<Coachmark>
): CoachmarkOverlay.State {
	val coachmark = pendingCoachmarks.firstOrNull()
		?: return withoutCoachmarks()

	return copy(
		activeCoachmark = coachmark,
		previousCoachmarks = previousCoachmarks,
		pendingCoachmarks = pendingCoachmarks
	)
}

private fun CoachmarkOverlay.State.moveToPreviousCoachmark(): CoachmarkOverlay.State {
	val previousCoachmark = previousCoachmarks.lastOrNull()
		?: return this

	return withPendingCoachmarks(
		previousCoachmarks = previousCoachmarks.dropLast(1),
		pendingCoachmarks = listOf(previousCoachmark) + pendingCoachmarks
	)
}
