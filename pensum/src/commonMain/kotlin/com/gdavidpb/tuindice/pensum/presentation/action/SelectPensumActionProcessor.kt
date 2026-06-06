package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.error.UpdatePensumUseCaseError
import com.gdavidpb.tuindice.pensum.domain.usecase.param.SelectPensumParams
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.mapper.failedOrContent
import com.gdavidpb.tuindice.pensum.presentation.mapper.loadingOrContent
import com.gdavidpb.tuindice.pensum.presentation.mapper.snackBarEffectOrNull
import com.gdavidpb.tuindice.pensum.presentation.mapper.toFailedMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

class SelectPensumActionProcessor(
	private val selectPensumUseCase: SelectPensumUseCase
) : ActionProcessor<Pensum.State, Pensum.Action.SelectPensum, Pensum.Effect>() {
	override suspend fun process(
		action: Pensum.Action.SelectPensum,
		sideEffect: (Pensum.Effect) -> Unit
	): Flow<Mutation<Pensum.State>> {
		return selectPensumUseCase.execute(
			SelectPensumParams(
				year = action.year
			)
		).mapNotNull { useCaseState ->
			when (useCaseState) {
				is UseCaseState.Loading -> suspend { state: Pensum.State -> state.loadingOrContent() }
				is UseCaseState.Data -> null
				is UseCaseState.Error -> suspend { state: Pensum.State ->
							if (useCaseState.error == UpdatePensumUseCaseError.NotFound) {
								Pensum.State.Empty
							} else {
								state.snackBarEffectOrNull(useCaseState.error)?.let(sideEffect)
								state.failedOrContent(useCaseState.error.toFailedMessage())
							}
						}
				}
		}
	}
}
