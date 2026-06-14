package com.gdavidpb.tuindice.base.presentation

import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText

interface ViewState {
	val topBarTitle: UiText get() = UiText.Empty
	val topBarConfig: TopBarConfig? get() = null
	val isTopBarVisible: Boolean get() = false
	val isBottomBarVisible: Boolean get() = false
}
