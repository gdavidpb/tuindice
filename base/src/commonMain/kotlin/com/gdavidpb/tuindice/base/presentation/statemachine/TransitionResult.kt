package com.gdavidpb.tuindice.base.presentation.statemachine

import com.gdavidpb.tuindice.base.presentation.ViewState

sealed class TransitionResult<S : ViewState> {
	class Transitioned<S : ViewState>(
		val toState: S,
		val transition: TransitionSpec<S>
	) : TransitionResult<S>()

	class Rejected<S : ViewState> : TransitionResult<S>()
}
