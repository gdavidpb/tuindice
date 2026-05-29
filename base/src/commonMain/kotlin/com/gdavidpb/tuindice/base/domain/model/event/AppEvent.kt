package com.gdavidpb.tuindice.base.domain.model.event

sealed class AppEvent private constructor(
	val name: String,
	val parameters: Map<String, String>
) {
	data class ScreenView(
		private val source: String
	) : AppEvent(
		name = EventNames.SCREEN_VIEW,
		parameters = mapOf(
			EventParameterKeys.SOURCE to source,
			EventParameterKeys.SCREEN_NAME to source
		)
	)

	data class Action(
		private val source: String,
		private val action: String
	) : AppEvent(
		name = EventNames.APP_ACTION,
		parameters = mapOf(
			EventParameterKeys.SOURCE to source,
			EventParameterKeys.ACTION to action
		)
	)

	data class State(
		private val source: String,
		private val state: String
	) : AppEvent(
		name = EventNames.APP_STATE,
		parameters = mapOf(
			EventParameterKeys.SOURCE to source,
			EventParameterKeys.STATE to state
		)
	)

	data class Effect(
		private val source: String,
		private val effect: String
	) : AppEvent(
		name = EventNames.APP_EFFECT,
		parameters = mapOf(
			EventParameterKeys.SOURCE to source,
			EventParameterKeys.EFFECT to effect
		)
	)
}
