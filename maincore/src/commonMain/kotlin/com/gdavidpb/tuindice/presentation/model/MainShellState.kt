package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarAction
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.presentation.mapper.arrangeTopBarActions
import com.gdavidpb.tuindice.record.presentation.model.RecordRouteViewState
import com.gdavidpb.tuindice.record.presentation.model.RecordTopBarViewModeState

data class MainShellState(
	val topBarTitle: UiText = UiText.Empty,
	val topBarConfig: TopBarConfig? = null,
	val isTopBarVisible: Boolean = false,
	val isBottomBarVisible: Boolean = false,
	val recordTopBarViewModeState: RecordTopBarViewModeState? = null,
	val showsTopBarBackButton: Boolean = true,
	// Whether the active screen declared a context for the assistant, and whether the assistant is on.
	// Only its presence is kept: the content changes with the screen (a scroll), and the shell must not follow it.
	val hasAssistantContext: Boolean = false,
	val isAssistantVisible: Boolean = false,
	val isProfileAvatarVisible: Boolean = false
) {
	// The places of the bar in view, and the actions in its ⋮ menu. The one priority table is in arrangeTopBarActions.
	// They are worked out once, when the state is made, because the bar reads them on every frame.
	val inlineActions: List<TopBarSlot>

	val overflowActions: List<TopBarAction>

	// The avatar that opens Settings ends the bar of Summary, the one section that declares it; the others keep their bar
	// for their own places. A screen pushed over a tab has the back button instead, and no avatar.
	val showsProfileAvatar: Boolean
		get() = isTopBarVisible && isBottomBarVisible && isProfileAvatarVisible

	init {
		val arrangement = arrangeTopBarActions(
			actions = topBarConfig?.actions.orEmpty(),
			hasAssistant = isAssistantVisible && hasAssistantContext,
			hasViewModeSelector = recordTopBarViewModeState != null
		)

		inlineActions = arrangement.first
		overflowActions = arrangement.second
	}
}

fun ViewState.toMainShellState(isAssistantVisible: Boolean = false): MainShellState = MainShellState(
	topBarTitle = topBarTitle,
	topBarConfig = topBarConfig,
	isTopBarVisible = isTopBarVisible,
	isBottomBarVisible = isBottomBarVisible,
	recordTopBarViewModeState = when (this) {
		is RecordRouteViewState -> topBarViewModeState
		else -> null
	},
	hasAssistantContext = assistantContext != null,
	isAssistantVisible = isAssistantVisible,
	isProfileAvatarVisible = isProfileAvatarVisible
)
