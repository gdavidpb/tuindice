package com.gdavidpb.tuindice.pensum.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.pensum.domain.usecase.SelectPensumSelectionUseCase
import com.gdavidpb.tuindice.pensum.domain.usecase.param.SelectPensumSelectionParams
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.mapper.toPensumRefreshMutation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
		).map { useCaseState -> useCaseState.toPensumRefreshMutation() }
	}
}
