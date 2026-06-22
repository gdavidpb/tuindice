package com.gdavidpb.tuindice.wizard.presentation.machine

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.wizard.domain.usecase.MarkCoachmarkSeenUseCase
import com.gdavidpb.tuindice.wizard.domain.usecase.ResolveCoachmarkUseCase
import com.gdavidpb.tuindice.wizard.presentation.contract.CoachmarkOverlay
import com.gdavidpb.tuindice.wizard.presentation.model.Coachmark
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkSurface
import com.gdavidpb.tuindice.wizard.presentation.model.contextualCoachmarks
import com.gdavidpb.tuindice.wizard.presentation.model.persistedId
import com.gdavidpb.tuindice.wizard.presentation.transition.coachmarkOverlayTransitions

class CoachmarkOverlayMachine(
	private val resolveCoachmarkUseCase: ResolveCoachmarkUseCase,
	private val markCoachmarkSeenUseCase: MarkCoachmarkSeenUseCase
) : ScreenMachine<CoachmarkOverlay.State, CoachmarkOverlay.Effect> {
	private val coachmarks = contextualCoachmarks()
	private val allCoachmarkIds = coachmarks.map { coachmark -> coachmark.id.persistedId }.toSet()

	override fun initialState(): CoachmarkOverlay.State = CoachmarkOverlay.State()

	override fun define(host: MachineHost<CoachmarkOverlay.Effect>):
		MachineDefinition<CoachmarkOverlay.State> {
		return MachineDefinition.define {
			coachmarkOverlayTransitions(machine = this@CoachmarkOverlayMachine, host = host)
		}
	}

	internal fun resolveCoachmark(
		host: MachineHost<CoachmarkOverlay.Effect>,
		surface: CoachmarkSurface
	) {
		host.launchMachineJob {
			resolveCoachmarkUseCase.execute(
				ResolveCoachmarkUseCase.Params(
					eligibleCoachmarkIds = surface.eligibleCoachmarkIds
						.map { coachmarkId -> coachmarkId.persistedId },
					allCoachmarkIds = allCoachmarkIds
				)
			).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Data -> host.processInternalEvent(
						CoachmarkOverlayInternalEvent.CoachmarkResolved(
							visitKey = surface.visitKey,
							resolution = useCaseState.value
						)
					)

					is UseCaseState.Loading,
					is UseCaseState.Error,
					-> Unit
				}
			}
		}
	}

	internal fun markCoachmarkSeen(
		host: MachineHost<CoachmarkOverlay.Effect>,
		coachmarkId: String
	) {
		host.launchMachineJob {
			markCoachmarkSeenUseCase.execute(
				MarkCoachmarkSeenUseCase.Params(
					coachmarkId = coachmarkId,
					allCoachmarkIds = allCoachmarkIds
				)
			).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Data,
					is UseCaseState.Loading,
					is UseCaseState.Error,
					-> Unit
				}
			}
		}
	}

	internal fun coachmarkFor(coachmarkId: String): Coachmark? =
		coachmarks.firstOrNull { coachmark -> coachmark.id.persistedId == coachmarkId }
}
