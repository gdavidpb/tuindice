package com.gdavidpb.tuindice.about.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.top_bar_about

object About {
	sealed class State(
		override val topBarTitle: UiText = UiText.Resource(Res.string.top_bar_about),
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = true
	) : ViewState() {
		data object Idle : State()
		data class Content(
			val versionText: String,
			val usageDataCollectionEnabled: Boolean = false
		) : State()
	}

	sealed class Action : ViewAction() {
		data object LoadVersion : Action()
		data object OpenTermsAndConditions : Action()
		data object OpenPrivacyPolicy : Action()
		data object OpenSupport : Action()
		data class OpenUrl(val url: String) : Action()
		data object RateOnStore : Action()
		data object ReportBug : Action()
		data object ContactDeveloper : Action()
		data object ShareApp : Action()
		data class SetUsageDataCollectionEnabled(val enabled: Boolean) : Action()
	}

	sealed class Effect : ViewEffect() {
		data class NavigateToBrowser(
			val title: String,
			val url: String
		) : Effect()

		data class OpenUri(
			val uri: String
		) : Effect()

		data class ShareText(
			val subject: String,
			val text: String
		) : Effect()
	}
}
