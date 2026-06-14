package com.gdavidpb.tuindice.record.presentation.model

import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText

class RecordRouteViewState(
	override val topBarTitle: UiText = UiText.Empty,
	override val topBarConfig: TopBarConfig? = null,
	override val isTopBarVisible: Boolean = false,
	override val isBottomBarVisible: Boolean = false,
	val topBarViewModeState: RecordTopBarViewModeState? = null
) : ViewState
