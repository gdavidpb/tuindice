package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.record.presentation.model.RecordRouteViewState
import com.gdavidpb.tuindice.record.presentation.model.RecordTopBarViewModeState

data class MainShellState(
	val topBarTitle: String = "",
	val topBarConfig: TopBarConfig? = null,
	val isTopBarVisible: Boolean = false,
	val isBottomBarVisible: Boolean = false,
	val recordTopBarViewModeState: RecordTopBarViewModeState? = null
)

fun ViewState.toMainShellState(): MainShellState = MainShellState(
	topBarTitle = topBarTitle,
	topBarConfig = topBarConfig,
	isTopBarVisible = isTopBarVisible,
	isBottomBarVisible = isBottomBarVisible,
	recordTopBarViewModeState = (this as? RecordRouteViewState)?.topBarViewModeState
)
