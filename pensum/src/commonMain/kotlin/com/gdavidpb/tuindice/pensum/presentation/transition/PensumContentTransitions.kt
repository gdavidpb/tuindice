package com.gdavidpb.tuindice.pensum.presentation.transition

import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.machine.PensumInternalEvent
import com.gdavidpb.tuindice.pensum.presentation.mapper.toLocalDataWarningMessage
import com.gdavidpb.tuindice.pensum.presentation.mapper.toScreenModel
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_selection_not_found

internal fun MachineDefinitionBuilder<Pensum.State>.contentTransitions() {
	from<Pensum.State.Content> {
		on<PensumInternalEvent.PensumContentObserved> { state, event ->
			state.copy(
				model = event.pensum.toScreenModel(),
				isSummaryCollapsed = event.isSummaryCollapsed
			)
		}

		on<PensumInternalEvent.PensumRefreshLoading> { state, _ ->
			state.copy(isRefreshing = true, localDataMessage = null)
		}

		on<PensumInternalEvent.PensumRefreshFailed> { state, event ->
			state.copy(
				isRefreshing = false,
				localDataMessage = event.error.toLocalDataWarningMessage()
			)
		}

		// A not-found selection was never persisted (the data layer validates it
		// before persisting), so the on-screen pensum is still the active one.
		on<PensumInternalEvent.PensumRefreshNotFound> { state, _ ->
			state.copy(
				isRefreshing = false,
				localDataMessage = UiText.Resource(Res.string.pensum_selection_not_found)
			)
		}
	}
}
