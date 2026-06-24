package com.gdavidpb.tuindice.pensum.presentation.transition

import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinitionBuilder
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.machine.PensumInternalEvent
import com.gdavidpb.tuindice.pensum.presentation.mapper.toLocalDataWarningMessage
import com.gdavidpb.tuindice.pensum.presentation.mapper.toScreenModel

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
	}
}
