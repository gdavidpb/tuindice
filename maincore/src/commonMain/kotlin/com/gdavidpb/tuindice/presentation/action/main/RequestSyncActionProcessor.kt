package com.gdavidpb.tuindice.presentation.action.main

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.action.ActionProcessor
import com.gdavidpb.tuindice.domain.usecase.ScheduleSyncUseCase
import com.gdavidpb.tuindice.presentation.contract.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RequestSyncActionProcessor(
	private val scheduleSyncUseCase: ScheduleSyncUseCase
) : ActionProcessor<Main.State, Main.Action.RequestSync, Main.Effect>() {
	override suspend fun process(
		action: Main.Action.RequestSync,
		sideEffect: (Main.Effect) -> Unit
	): Flow<Mutation<Main.State>> {
		return scheduleSyncUseCase.execute(params = Unit)
			.map {
				suspend { state -> state }
			}
	}
}
