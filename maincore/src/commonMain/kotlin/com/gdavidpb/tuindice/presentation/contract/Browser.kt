package com.gdavidpb.tuindice.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText

object Browser {
	sealed class State : ViewState {
		data object Idle : State()

		data class Content(
			override val topBarTitle: UiText,
			override val isTopBarVisible: Boolean = true,
			val url: String,
			val isLoading: Boolean,
			val hasError: Boolean = false,
			// Bumped on retry so the screen can rebuild the platform web view.
			val reloadKey: Int = 0
		) : State()
	}

	sealed class Action : ViewAction {
		class NavigateTo(val title: String, val url: String) : Action()
		class SetLoading(val isLoading: Boolean) : Action()
		data object SetLoadFailed : Action()
		data object ClickRetry : Action()
		class OpenExternalResource(val url: String) : Action()
	}

	sealed class Effect : ViewEffect {
		class NavigateToExternalResourceDialog(val url: String) : Effect()
	}
}
