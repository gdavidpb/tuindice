package com.gdavidpb.tuindice.record.presentation.model

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText

class RecordRouteViewState(
	override val topBarTitle: UiText = UiText.Empty,
	override val topBarConfig: TopBarConfig? = null,
	override val isTopBarVisible: Boolean = false,
	override val isBottomBarVisible: Boolean = false,
	// What the record declared for the assistant. The bar reads it from here, so a route state that left it out
	// would take the assistant off the record's bar.
	override val assistantContext: AssistantScreenContext? = null,
	val topBarViewModeState: RecordTopBarViewModeState? = null
) : ViewState
