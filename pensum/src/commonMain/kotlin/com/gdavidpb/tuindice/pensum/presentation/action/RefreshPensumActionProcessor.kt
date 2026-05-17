package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.pensum.domain.usecase.UpdatePensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class RefreshPensumActionProcessor(
	private val updatePensumUseCase: UpdatePensumUseCase
) : ActionProcessor<Pensum.State, Pensum.Action.RefreshPensum, Pensum.Effect>() {
	override suspend fun process(
		action: Pensum.Action.RefreshPensum,
		sideEffect: (Pensum.Effect) -> Unit
	): Flow<Mutation<Pensum.State>> {
		return updatePensumUseCase.execute(Unit)
			.mapNotNull { useCaseState ->
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

internal fun Pensum.State.loadingOrContent(): Pensum.State = when (this) {
	is Pensum.State.Content -> this
	Pensum.State.Empty,
	Pensum.State.Failed,
	Pensum.State.Idle,
	Pensum.State.Loading,
	-> Pensum.State.Loading
}

internal fun Pensum.State.failedOrContent(): Pensum.State = when (this) {
	is Pensum.State.Content -> this
	Pensum.State.Empty,
	Pensum.State.Failed,
	Pensum.State.Idle,
	Pensum.State.Loading,
	-> Pensum.State.Failed
}
