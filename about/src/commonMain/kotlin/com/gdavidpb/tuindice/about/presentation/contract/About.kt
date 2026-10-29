package com.gdavidpb.tuindice.about.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.about.generated.resources.Res
import tuindice.about.generated.resources.top_bar_about

// «Acerca de TuIndice», pushed from Settings: the version, the license, the developer, the libraries and the thanks.
object About {
	sealed class State(
		override val topBarTitle: UiText = UiText.Resource(Res.string.top_bar_about),
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = false
	) : ViewState {
		data object Idle : State()
		data class Content(
			val versionText: String
		) : State()
	}

	sealed class Action : ViewAction {
		data object LoadVersion : Action()
		data class OpenUrl(val url: String) : Action()
		data object ContactDeveloper : Action()
	}

	sealed class Effect : ViewEffect {
		data class OpenUri(
			val uri: String
		) : Effect()
	}
}
