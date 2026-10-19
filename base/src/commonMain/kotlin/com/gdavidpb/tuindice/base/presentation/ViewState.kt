package com.gdavidpb.tuindice.base.presentation

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText

interface ViewState {
	val topBarTitle: UiText get() = UiText.Empty
	val topBarConfig: TopBarConfig? get() = null
	val isTopBarVisible: Boolean get() = false
	val isBottomBarVisible: Boolean get() = false
	val assistantContext: AssistantScreenContext? get() = null

	// Whether the bar ends with the student's photo that opens Settings. Only Summary, the hub, declares it: the other
	// sections keep their bar for their own actions.
	val isProfileAvatarVisible: Boolean get() = false
}
