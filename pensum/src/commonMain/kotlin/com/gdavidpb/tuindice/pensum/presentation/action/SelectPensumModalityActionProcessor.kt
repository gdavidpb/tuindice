package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumModalityUseCase
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.mapper.toPensumRefreshMutation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SelectPensumModalityActionProcessor(
	private val selectPensumModalityUseCase: SelectPensumModalityUseCase
) : ActionProcessor<Pensum.State, Pensum.Action.SelectModality, Pensum.Effect>() {
	override suspend fun process(
		action: Pensum.Action.SelectModality,
		sideEffect: (Pensum.Effect) -> Unit
	): Flow<Mutation<Pensum.State>> {
		return selectPensumModalityUseCase.execute(action.modalityId)
			.map { useCaseState -> useCaseState.toPensumRefreshMutation() }
	}
}
