package com.gdavidpb.tuindice.base.data.source.event

import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.model.event.EventParameterKeys
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.gdavidpb.tuindice.base.domain.repository.ReportingRepository

class ReportingBreadcrumbEventSubscriber(
	private val reportingRepository: ReportingRepository
) : EventSubscriber {
	override val id: String = "crashlytics_breadcrumbs"

	override val isEnabled: Boolean = true

	override fun onEvent(event: AppEvent) {
		if (!event.isAnalyticsRelevant()) return

		reportingRepository.logMessage(event.toBreadcrumbMessage())
		event.setBreadcrumbContext()
	}

	private fun AppEvent.setBreadcrumbContext() {
		parameter(EventParameterKeys.SOURCE)?.let { source ->
			reportingRepository.setCustomKey(CURRENT_SCREEN_KEY, source)
		}

		when (this) {
			is AppEvent.ScreenView -> Unit

			is AppEvent.Action ->
				parameter(EventParameterKeys.ACTION)?.let { action ->
					reportingRepository.setCustomKey(LAST_ACTION_KEY, action)
				}

			is AppEvent.State ->
				parameter(EventParameterKeys.STATE)?.let { state ->
					reportingRepository.setCustomKey(CURRENT_STATE_KEY, state)
				}

			is AppEvent.Effect ->
				parameter(EventParameterKeys.EFFECT)?.let { effect ->
					reportingRepository.setCustomKey(LAST_EFFECT_KEY, effect)
				}

			is AppEvent.Transition -> {
				val transition = transitionValue()
				if (transition != null) {
					reportingRepository.setCustomKey(LAST_TRANSITION_KEY, transition)
				}
				parameter(EventParameterKeys.TO)?.let { state ->
					reportingRepository.setCustomKey(CURRENT_STATE_KEY, state)
				}
			}

			is AppEvent.InvalidTransition ->
				invalidTransitionValue()?.let { transition ->
					reportingRepository.setCustomKey(LAST_INVALID_TRANSITION_KEY, transition)
				}
		}
	}
}

private fun AppEvent.toBreadcrumbMessage(): String {
	return buildString {
		append("mvi ")
		append(name)
		parameters.forEach { (key, value) ->
			append(' ')
			append(key)
			append('=')
			append(value)
		}
	}
}

private fun AppEvent.transitionValue(): String? {
	return formatBreadcrumbValue(
		EventParameterKeys.FROM,
		EventParameterKeys.EVENT,
		EventParameterKeys.TO
	)
}

private fun AppEvent.invalidTransitionValue(): String? {
	return formatBreadcrumbValue(
		EventParameterKeys.FROM,
		EventParameterKeys.EVENT
	)
}

private fun AppEvent.formatBreadcrumbValue(vararg keys: String): String? {
	val values = keys.map { key ->
		key to (parameter(key) ?: return null)
	}

	return values.joinToString(separator = " ") { (key, value) ->
		"$key=$value"
	}
}

private fun AppEvent.parameter(key: String): String? = parameters[key]

private const val CURRENT_SCREEN_KEY = "mvi.current_screen"
private const val CURRENT_STATE_KEY = "mvi.current_state"
private const val LAST_ACTION_KEY = "mvi.last_action"
private const val LAST_EFFECT_KEY = "mvi.last_effect"
private const val LAST_TRANSITION_KEY = "mvi.last_transition"
private const val LAST_INVALID_TRANSITION_KEY = "mvi.last_invalid_transition"
