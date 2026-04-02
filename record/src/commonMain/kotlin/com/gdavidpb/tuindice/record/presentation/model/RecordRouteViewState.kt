package com.gdavidpb.tuindice.record.presentation.model

import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig

class RecordRouteViewState(
	topBarTitle: String = "",
	topBarConfig: TopBarConfig? = null,
	isTopBarVisible: Boolean = false,
	isBottomBarVisible: Boolean = false,
	val topBarViewModeState: RecordTopBarViewModeState? = null
) : ViewState(
	topBarTitle = topBarTitle,
	topBarConfig = topBarConfig,
	isTopBarVisible = isTopBarVisible,
	isBottomBarVisible = isBottomBarVisible
)
