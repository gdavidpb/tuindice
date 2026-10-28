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
	// What the record declared for the assistant, or nothing. The shell reads whether it declared one on each state,
	// without projecting it. A route state that left it out would take the assistant off the record's bar.
	private val declaredAssistantContext: RecordAssistantDeclaration? = null,
	val topBarViewModeState: RecordTopBarViewModeState? = null
) : ViewState {
	override val hasAssistantContext: Boolean
		get() = declaredAssistantContext != null

	override val assistantContext: AssistantScreenContext?
		get() = declaredAssistantContext?.context
}
