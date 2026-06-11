package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.pensum.domain.usecase.UpdatePensumUseCase
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.mapper.toPensumRefreshMutation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RefreshPensumActionProcessor(
	private val updatePensumUseCase: UpdatePensumUseCase
) : ActionProcessor<Pensum.State, Pensum.Action.RefreshPensum, Pensum.Effect> {
	override suspend fun process(
		action: Pensum.Action.RefreshPensum,
		sideEffect: (Pensum.Effect) -> Unit
	): Flow<Mutation<Pensum.State>> {
		return updatePensumUseCase.execute(Unit)
			.map { useCaseState -> useCaseState.toPensumRefreshMutation() }
	}
}
