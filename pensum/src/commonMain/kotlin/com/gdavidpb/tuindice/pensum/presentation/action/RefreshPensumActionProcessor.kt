package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.base.presentation.model.UiText
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
							state.showSnackBarIfContent(useCaseState.error, sideEffect)
							state.failedOrContent(useCaseState.error.toFailedMessage())
						}
					}
				}
			}
	}
}

internal fun Pensum.State.loadingOrContent(): Pensum.State = when (this) {
	is Pensum.State.Content -> this
	Pensum.State.Empty,
	is Pensum.State.Failed,
	Pensum.State.Idle,
	Pensum.State.Loading,
	-> Pensum.State.Loading
}

internal fun Pensum.State.failedOrContent(message: UiText): Pensum.State = when (this) {
	is Pensum.State.Content -> this
	Pensum.State.Empty,
	is Pensum.State.Failed,
	Pensum.State.Idle,
	Pensum.State.Loading,
	-> Pensum.State.Failed(message = message)
}

internal suspend fun Pensum.State.showSnackBarIfContent(
	error: UpdatePensumUseCaseError?,
	sideEffect: (Pensum.Effect) -> Unit
) {
	if (this is Pensum.State.Content) {
		sideEffect(Pensum.Effect.ShowSnackBar(error.toSnackBarMessage()))
	}
}
