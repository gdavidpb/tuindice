package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumModalityUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class SelectPensumModalityActionProcessor(
	private val selectPensumModalityUseCase: SelectPensumModalityUseCase
) : ActionProcessor<Pensum.State, Pensum.Action.SelectModality, Pensum.Effect>() {
	override suspend fun process(
		action: Pensum.Action.SelectModality,
		sideEffect: (Pensum.Effect) -> Unit
	): Flow<Mutation<Pensum.State>> {
		return selectPensumModalityUseCase.execute(action.modalityId)
			.mapNotNull { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> suspend { state: Pensum.State -> state.loadingOrContent() }
					is UseCaseState.Data -> null
					is UseCaseState.Error -> suspend { state: Pensum.State ->
							if (useCaseState.error == UpdatePensumUseCaseError.NotFound) {
								Pensum.State.Empty
							} else {
								state.showSnackBarIfContent(useCaseState.error, sideEffect)
								state.failedOrContent(useCaseState.error.toFailedMessage())
							}
						}
					}
			}
	}
}
