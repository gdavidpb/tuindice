package com.gdavidpb.tuindice.pensum.presentation.transition

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.machine.PensumInternalEvent
import com.gdavidpb.tuindice.pensum.presentation.machine.PensumMachine
import com.gdavidpb.tuindice.pensum.presentation.mapper.toFailedMessage
import com.gdavidpb.tuindice.pensum.presentation.mapper.toScreenModel
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_failed_message

internal fun MachineDefinitionBuilder<Pensum.State>.anyStateTransitions(
	machine: PensumMachine,
	host: MachineHost<Pensum.Effect>
) {
	fromAny {
		on<Pensum.Action.EnsurePensumLoaded> { state, _ -> state }

		on<Pensum.Action.RefreshPensum> { state, _ ->
			machine.refreshPensum(host = host)
			state
		}

		on<Pensum.Action.SelectPensum> { state, action ->
			machine.selectPensum(host = host, year = action.year)
			state
		}

		on<Pensum.Action.SelectModality> { state, action ->
			machine.selectModality(host = host, modalityId = action.modalityId)
			state
		}

		on<Pensum.Action.SelectSelection> { state, action ->
			machine.selectSelection(
				host = host,
				year = action.year,
				modalityId = action.modalityId
			)
			state
		}

		on<Pensum.Action.ToggleSummaryCollapsed> { state, _ ->
			if (state is Pensum.State.Content) {
				val nextCollapsed = !state.isSummaryCollapsed
				machine.setSummaryCollapsed(
					host = host,
					isCollapsed = nextCollapsed
				)
				state.copy(isSummaryCollapsed = nextCollapsed)
			} else {
				state
			}
		}

		onTo<PensumInternalEvent.PensumContentObserved, Pensum.State.Content> { _, event ->
			Pensum.State.Content(
				model = event.pensum.toScreenModel(),
				isSummaryCollapsed = event.isSummaryCollapsed
			)
		}

		on<PensumInternalEvent.PensumDataMissing> { state, _ -> state }

		onTo<PensumInternalEvent.PensumRecordDataUnavailableObserved, Pensum.State.RecordDataUnavailable> { _, _ ->
			Pensum.State.RecordDataUnavailable
		}

		onTo<PensumInternalEvent.PensumObservationFailed, Pensum.State.Failed> { _, _ ->
			Pensum.State.Failed(
				message = UiText.Resource(Res.string.pensum_failed_message)
			)
		}

		onTo<PensumInternalEvent.PensumRefreshLoading, Pensum.State.Loading> { _, _ ->
			Pensum.State.Loading
		}

		on<PensumInternalEvent.PensumRefreshSucceeded> { state, _ ->
			when (state) {
				is Pensum.State.Content ->
					state.copy(isRefreshing = false, localDataMessage = null)

				else -> state
			}
		}

		onTo<PensumInternalEvent.PensumRefreshNotFound, Pensum.State.Empty> { _, _ ->
			Pensum.State.Empty
		}

		onTo<PensumInternalEvent.PensumRefreshFailed, Pensum.State.Failed> { _, event ->
			Pensum.State.Failed(message = event.error.toFailedMessage())
		}
	}
}
