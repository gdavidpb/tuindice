package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
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
	// What the active screen declared for the assistant, and whether the assistant is on.
	val assistantContext: AssistantScreenContext? = null,
	val isAssistantVisible: Boolean = false
) {
	private val arrangement: Pair<List<TopBarSlot>, List<TopBarAction>>
		get() = arrangeTopBarActions(
			actions = topBarConfig?.actions.orEmpty(),
			hasAssistant = isAssistantVisible && assistantContext != null,
			hasViewModeSelector = recordTopBarViewModeState != null
		)

	// The places of the bar in view, and the actions in its ⋮ menu. The one priority table is in arrangeTopBarActions.
	val inlineActions: List<TopBarSlot>
		get() = arrangement.first

	val overflowActions: List<TopBarAction>
		get() = arrangement.second
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
	assistantContext = assistantContext,
	isAssistantVisible = isAssistantVisible
)
