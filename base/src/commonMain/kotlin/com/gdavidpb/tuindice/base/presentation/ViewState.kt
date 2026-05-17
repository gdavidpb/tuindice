package com.gdavidpb.tuindice.base.presentation

import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText

abstract class ViewState(
	open val topBarTitle: UiText = UiText.Empty,
	open val topBarConfig: TopBarConfig? = null,
	open val isTopBarVisible: Boolean = false,
	open val isBottomBarVisible: Boolean = false
)
