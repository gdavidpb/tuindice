package com.gdavidpb.tuindice.record.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.domain.model.quarter.Quarter
import com.gdavidpb.tuindice.record.domain.usecase.GetSelectedQuarterIdUseCase
import com.gdavidpb.tuindice.record.domain.usecase.ObserveQuartersUseCase
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

						if (quarters.isNotEmpty())
							Record.State.Content(
								quarters = quarters,
								selectedQuarterId = resolveSelectedQuarterId(quarters = quarters)
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

	private suspend fun resolveSelectedQuarterId(quarters: List<Quarter>): String {
		val persistedSelectedQuarterId = when (
			val selectionState = getSelectedQuarterIdUseCase.execute(Unit).last()
		) {
			is UseCaseState.Data -> selectionState.value
			else -> null
		}
		val resolvedSelectedQuarterId = quarters
			.firstOrNull { quarter -> quarter.id == persistedSelectedQuarterId }
			?.id
			?: quarters.first().id

		if (resolvedSelectedQuarterId != persistedSelectedQuarterId) {
			setSelectedQuarterIdUseCase.execute(resolvedSelectedQuarterId).last()
		}

		return resolvedSelectedQuarterId
	}
}
