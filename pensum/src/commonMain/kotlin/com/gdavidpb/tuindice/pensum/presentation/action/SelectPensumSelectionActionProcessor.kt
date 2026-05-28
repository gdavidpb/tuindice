package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumSelectionUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.domain.usecase.param.SelectPensumSelectionParams
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class SelectPensumSelectionActionProcessor(
	private val selectPensumSelectionUseCase: SelectPensumSelectionUseCase
) : ActionProcessor<Pensum.State, Pensum.Action.SelectSelection, Pensum.Effect>() {
	override suspend fun process(
		action: Pensum.Action.SelectSelection,
		sideEffect: (Pensum.Effect) -> Unit
	): Flow<Mutation<Pensum.State>> {
		return selectPensumSelectionUseCase.execute(
			SelectPensumSelectionParams(
				year = action.year,
				modalityId = action.modalityId
			)
		).mapNotNull { useCaseState ->
			when (useCaseState) {
				is UseCaseState.Loading -> suspend { state: Pensum.State -> state.loadingOrContent() }
				is UseCaseState.Data -> null
				is UseCaseState.Error -> suspend { state: Pensum.State ->
					if (useCaseState.error == UpdatePensumUseCaseError.NotFound) {
						Pensum.State.Empty
					} else {
						sideEffect(Pensum.Effect.ShowSnackBar(useCaseState.error.toSnackBarMessage()))
						state.failedOrContent()
					}
				}
			}
		}
	}
}
