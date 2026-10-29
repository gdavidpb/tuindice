package com.gdavidpb.tuindice.base.domain.model.assistant

data class AssistantAvailability(
	val mode: AssistantMode,
	val serverDisabled: Boolean
) {
	// The server switch wins over the configured mode.
	val effectiveMode: AssistantMode
		get() = if (serverDisabled) AssistantMode.OFF else mode

	val isVisible: Boolean
		get() = effectiveMode != AssistantMode.OFF
}
