package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.other
import com.gdavidpb.tuindice.record.domain.model.filterByViewMode
import com.gdavidpb.tuindice.record.domain.usecase.GetSelectedQuarterIdUseCase
import com.gdavidpb.tuindice.record.domain.usecase.GetRecordViewModeUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveQuartersUseCase
import com.gdavidpb.tuindice.record.domain.usecase.param.SetSelectedQuarterIdParams
import com.gdavidpb.tuindice.record.domain.usecase.SetSelectedQuarterIdUseCase
import com.gdavidpb.tuindice.record.presentation.contract.Record
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import org.jetbrains.compose.resources.getString
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.snack_default_error

class ObserveQuartersActionProcessor(
	private val observeQuartersUseCase: ObserveQuartersUseCase,
	private val getRecordViewModeUseCase: GetRecordViewModeUseCase,
	private val getSelectedQuarterIdUseCase: GetSelectedQuarterIdUseCase,
	private val setSelectedQuarterIdUseCase: SetSelectedQuarterIdUseCase
) : ActionProcessor<Record.State, Record.Action.ObserveQuarters, Record.Effect>() {

	override suspend fun process(
		action: Record.Action.ObserveQuarters,
		sideEffect: (Record.Effect) -> Unit
	): Flow<Mutation<Record.State>> {
		return observeQuartersUseCase.execute(Unit)
			.mapNotNull { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> null

					is UseCaseState.Data -> suspend { state: Record.State ->
						val quarters = useCaseState.value
						val viewMode = resolveRecordViewMode()

						if (quarters.isNotEmpty())
							Record.State.Content(
								quarters = quarters,
								viewMode = viewMode,
								selectedQuarterId = resolveSelectedQuarterId(
									quarters = quarters,
									viewMode = viewMode,
									preferredQuarterId = (state as? Record.State.Content)?.selectedQuarterId
								)
							)
						else if (state is Record.State.Loading)
							state
						else
							Record.State.Empty
					}

					is UseCaseState.Error -> suspend { _: Record.State ->
						sideEffect(
							Record.Effect.ShowSnackBar(
								message = getString(Res.string.snack_default_error)
							)
						)

						Record.State.Failed
					}
				}
			}
	}

	private suspend fun resolveRecordViewMode(): RecordViewMode {
		return when (val viewModeState = getRecordViewModeUseCase.execute(Unit).last()) {
			is UseCaseState.Data -> viewModeState.value
			else -> RecordViewMode.Simulation
		}
	}

	private suspend fun resolveSelectedQuarterId(
		quarters: List<Quarter>,
		viewMode: RecordViewMode,
		preferredQuarterId: String?
	): String {
		val visibleQuarters = quarters.filterByViewMode(viewMode)
		val persistedSelectedQuarterId = getPersistedSelectedQuarterId(viewMode = viewMode)
		val mirroredSelectedQuarterId = getPersistedSelectedQuarterId(viewMode = viewMode.other())
		val resolvedSelectedQuarterId = listOf(
			preferredQuarterId,
			persistedSelectedQuarterId,
			mirroredSelectedQuarterId
		).firstOrNull { quarterId ->
			visibleQuarters.any { quarter -> quarter.id == quarterId }
		}
			?: visibleQuarters.firstOrNull()?.id
			?: preferredQuarterId
			?: persistedSelectedQuarterId
			?: mirroredSelectedQuarterId
			?: quarters.first().id

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
