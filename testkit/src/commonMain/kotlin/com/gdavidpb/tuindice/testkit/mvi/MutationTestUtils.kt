package com.gdavidpb.tuindice.testkit.mvi

import com.gdavidpb.tuindice.base.presentation.Mutation

fun <S> List<Mutation<S>>.reduceMutations(initialState: S): S {
	return fold(initialState) { state, mutation -> mutation(state) }
}
