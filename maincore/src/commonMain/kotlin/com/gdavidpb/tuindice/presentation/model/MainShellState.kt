package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig

data class MainShellState(
	val topBarTitle: String = "",
	val topBarConfig: TopBarConfig? = null,
	val isTopBarVisible: Boolean = false,
	val isBottomBarVisible: Boolean = false
)

fun ViewState.toMainShellState(): MainShellState = MainShellState(
	topBarTitle = topBarTitle,
	topBarConfig = topBarConfig,
	isTopBarVisible = isTopBarVisible,
	isBottomBarVisible = isBottomBarVisible
)
