package com.gdavidpb.tuindice.base.presentation.contract

import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState

object Browser {
	sealed class State : ViewState {
		object Idle : State()
		data class Content(
			val title: String,
			val url: String,
			val isLoading: Boolean
		) : State()
	}

	sealed class Action : ViewAction {
		class OpenExternalResource(val url: String) : Action()
		class ConfirmOpenExternalResource(val url: String) : Action()
		object CloseDialog : Action()
	}

	sealed class Event : ViewEvent {
		class ShowExternalResourceDialog(val url: String) : Event()
		class OpenExternalResourceDialog(val url: String) : Event()
		object CloseDialog : Event()
	}
}