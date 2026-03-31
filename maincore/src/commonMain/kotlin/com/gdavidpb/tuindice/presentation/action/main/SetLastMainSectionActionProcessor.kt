package com.gdavidpb.tuindice.presentation.action.main

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.domain.usecase.SetLastMainSectionUseCase
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SetLastMainSectionActionProcessor(
	private val setLastMainSectionUseCase: SetLastMainSectionUseCase
) : ActionProcessor<Main.State, Main.Action.SetLastMainSection, Main.Effect>() {

	override suspend fun process(
		action: Main.Action.SetLastMainSection,
		sideEffect: (Main.Effect) -> Unit
	): Flow<Mutation<Main.State>> {
		return setLastMainSectionUseCase.execute(params = action.section)
			.map { _ -> { state -> state } }
	}
}
