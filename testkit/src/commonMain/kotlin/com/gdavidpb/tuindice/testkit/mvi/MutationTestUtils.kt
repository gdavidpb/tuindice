package com.gdavidpb.tuindice.testkit.mvi

import com.gdavidpb.tuindice.base.presentation.Mutation

suspend fun <S> List<Mutation<S>>.reduceMutations(initialState: S): S {
	var state = initialState

	for (mutation in this) {
		state = mutation(state)
	}

	return state
}
