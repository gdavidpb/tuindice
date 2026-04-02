package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.filterByViewMode
import com.gdavidpb.tuindice.record.domain.model.other
import com.gdavidpb.tuindice.record.domain.usecase.GetSelectedQuarterIdUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedQuarterIdUseCase
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSelectedQuarterIdParams
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.last

class SetRecordViewModeActionProcessor(
	private val setRecordViewModeUseCase: SetRecordViewModeUseCase,
	private val getSelectedQuarterIdUseCase: GetSelectedQuarterIdUseCase,
	private val setSelectedQuarterIdUseCase: SetSelectedQuarterIdUseCase
) : ActionProcessor<Record.State, Record.Action.SetViewMode, Record.Effect>() {

	override suspend fun process(
		action: Record.Action.SetViewMode,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return flowOf(
			suspend { state: Record.State ->
				when (state) {
					is Record.State.Content -> {
						setRecordViewModeUseCase.execute(action.viewMode).last()

						val selectedQuarterId = resolveSelectedQuarterId(
							quarters = state.quarters,
							viewMode = action.viewMode,
							fallbackQuarterId = state.selectedQuarterId
						)

						state.copy(
							viewMode = action.viewMode,
							selectedQuarterId = selectedQuarterId
						)
					}

					Record.State.Empty,
					Record.State.Failed,
					Record.State.Loading,
					-> state
				}
			}
		)
	}

	private suspend fun resolveSelectedQuarterId(
		quarters: List<Quarter>,
		viewMode: RecordViewMode,
		fallbackQuarterId: String
	): String {
		val visibleQuarters = quarters.filterByViewMode(viewMode)
		val persistedSelectedQuarterId = getPersistedSelectedQuarterId(viewMode = viewMode)
		val mirroredSelectedQuarterId = getPersistedSelectedQuarterId(viewMode = viewMode.other())
		val resolvedSelectedQuarterId = listOfNotNull(
			fallbackQuarterId,
			persistedSelectedQuarterId,
			mirroredSelectedQuarterId
		).firstOrNull { quarterId ->
			visibleQuarters.any { quarter -> quarter.id == quarterId }
		}
			?: visibleQuarters.firstOrNull()?.id
			?: fallbackQuarterId

		if (
			visibleQuarters.isNotEmpty() &&
			(resolvedSelectedQuarterId != persistedSelectedQuarterId)
		) {
			setSelectedQuarterIdUseCase.execute(
				SetSelectedQuarterIdParams(
					viewMode = viewMode,
					quarterId = resolvedSelectedQuarterId
				)
			).last()
		}

		return resolvedSelectedQuarterId
	}

	private suspend fun getPersistedSelectedQuarterId(viewMode: RecordViewMode): String? {
		return when (val selectionState = getSelectedQuarterIdUseCase.execute(viewMode).last()) {
			is UseCaseState.Data -> selectionState.value
			else -> null
		}
	}
}
