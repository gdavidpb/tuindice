package com.gdavidpb.tuindice.about.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState

object About {
	sealed class State : ViewState {
		data object Idle : State()
	}

	sealed class Action : ViewAction {
		data object OpenTermsAndConditions : Action()
		data object OpenPrivacyPolicy : Action()
		class OpenUrl(val url: String) : Action()
		data object RateOnPlayStore : Action()
		data object ReportBug : Action()
		data object ContactDeveloper : Action()
		data object ShareApp : Action()
	}

	sealed class Effect : ViewEffect {
		class NavigateToBrowser(
			val title: String,
			val url: String
		) : Effect()

		class StartShare(
			val subject: String,
			val text: String
		) : Effect()

		data object StartPlayStore : Effect()
		data object StartEmail : Effect()

		class StartBrowser(
			val url: String
		) : Effect()

		data object ShowReportBugDialog : Effect()
	}
}