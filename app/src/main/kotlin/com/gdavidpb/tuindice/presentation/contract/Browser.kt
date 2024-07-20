package com.gdavidpb.tuindice.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState

object Browser {
	sealed class State : ViewState() {
		data object Idle : State()

		data class Content(
			val url: String,
			val isLoading: Boolean
		) : State()
	}

	sealed class Action : ViewAction() {
		class NavigateTo(val url: String) : Action()
		class SetLoading(val isLoading: Boolean) : Action()
		class OpenExternalResource(val url: String) : Action()
	}

	sealed class Effect : ViewEffect() {
		class NavigateToExternalResourceDialog(val url: String) : Effect()
	}
}