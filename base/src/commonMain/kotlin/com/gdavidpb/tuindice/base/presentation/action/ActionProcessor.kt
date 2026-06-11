package com.gdavidpb.tuindice.base.presentation.action

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import kotlinx.coroutines.flow.Flow

interface ActionProcessor<S : ViewState, A : ViewAction, E : ViewEffect> {
	suspend fun process(
		action: A,
		sideEffect: (E) -> Unit
	): Flow<Mutation<S>>
}
