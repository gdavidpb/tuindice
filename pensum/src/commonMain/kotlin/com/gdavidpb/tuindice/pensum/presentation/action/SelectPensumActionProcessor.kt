package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.param.SelectPensumParams
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.mapper.toPensumRefreshMutation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SelectPensumActionProcessor(
	private val selectPensumUseCase: SelectPensumUseCase
) : ActionProcessor<Pensum.State, Pensum.Action.SelectPensum, Pensum.Effect> {
	override suspend fun process(
		action: Pensum.Action.SelectPensum,
		sideEffect: (Pensum.Effect) -> Unit
	): Flow<Mutation<Pensum.State>> {
		return selectPensumUseCase.execute(
			SelectPensumParams(
				year = action.year
			)
		).map { useCaseState -> useCaseState.toPensumRefreshMutation() }
	}
}
