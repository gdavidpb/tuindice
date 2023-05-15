package com.gdavidpb.tuindice.about.presentation.contract

import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState

object About {
	sealed class State : ViewState {
		object Idle : State()
	}

	sealed class Action : ViewAction {
		object OpenTermsAndConditions : Action()
		object OpenPrivacyPolicy : Action()
		class OpenUrl(val url: String) : Action()
		object RateOnPlayStore : Action()
		object ReportBug : Action()
		object ContactDeveloper : Action()
		object ShareApp : Action()
	}

	sealed class Event : ViewEvent {
		class NavigateToBrowser(val title: String, val url: String) : Event()
		class StartShare(val subject: String, val text: String) : Event()
		object StartPlayStore : Event()
		object StartEmail : Event()
		class StartBrowser(val url: String) : Event()
		object ShowReportBugDialog : Event()
	}
}