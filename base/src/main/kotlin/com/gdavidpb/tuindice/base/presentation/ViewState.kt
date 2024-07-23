package com.gdavidpb.tuindice.base.presentation

import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig

abstract class ViewState(
	open val topBarTitle: String = "",
	open val topBarConfig: TopBarConfig? = null,
	open val isTopBarVisible: Boolean = false,
	open val isBottomBarVisible: Boolean = false
)