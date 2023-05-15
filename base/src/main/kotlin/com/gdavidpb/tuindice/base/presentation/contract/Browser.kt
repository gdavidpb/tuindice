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
			val isLoading: Boolean,
			val externalResourceUrl: String = "",
			val isShowExternalResourceDialog: Boolean = false
		) : State()
	}

	sealed class Action : ViewAction {
		class OpenUrl(val title: String, val url: String) : Action()
		class ClickExternalResource(val url: String) : Action()
		object CloseExternalResourceDialog : Action()
	}

	sealed class Event : ViewEvent
}