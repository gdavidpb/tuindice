package com.gdavidpb.tuindice.about.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.ProfileIdentity
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.top_bar_settings

object Settings {
	sealed class State(
		override val topBarTitle: UiText = UiText.Resource(Res.string.top_bar_settings),
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = false
	) : ViewState {
		data object Idle : State()
		data class Content(
			// Null until the stored user speaks (and again if none is stored): the card shows nothing of the student then.
			val identity: ProfileIdentity?,
			val versionText: String,
			val usageDataCollectionEnabled: Boolean = false,
			val assistantAvailable: Boolean = false,
			val assistantConsentGranted: Boolean = false
		) : State()
	}

	sealed class Action : ViewAction {
		data object LoadSettings : Action()
		data object OpenTermsAndConditions : Action()
		data object OpenPrivacyPolicy : Action()
		data object OpenSupport : Action()
		data class OpenUrl(val url: String) : Action()
		data object RateOnStore : Action()
		data object ReportBug : Action()
		data object ShareApp : Action()
		data object OpenAbout : Action()
		data object RequestSignOut : Action()
		data class SetUsageDataCollectionEnabled(val enabled: Boolean) : Action()
		data class SetAssistantConsent(val granted: Boolean) : Action()
		data object OpenAssistantMemory : Action()
		data object RequestDeleteAssistantConversations : Action()
		data object ConfirmDeleteAssistantConversations : Action()
	}

	sealed class Effect : ViewEffect {
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

		data object NavigateToAbout : Effect()

		// The host owns the sign-out flow (its pending-changes check and its confirmation), so the screen only asks.
		data object RequestSignOut : Effect()

		data object NavigateToAssistantMemory : Effect()

		data object NavigateToDeleteAssistantConversationsConfirmation : Effect()

		data class ShowSnackBar(
			val message: String
		) : Effect()
	}
}
