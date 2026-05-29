package com.gdavidpb.tuindice.base.data.source.event

import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.gdavidpb.tuindice.base.logging.appLogger

class CompositeEventSubscriber(
	private val subscribers: List<EventSubscriber>
) : EventSubscriber {
	private val logger = appLogger(tag = "AppEvents")

	override val id: String = "composite"

	override val isEnabled: Boolean
		get() = subscribers.any { subscriber -> subscriber.isEnabled }

	override fun onEvent(event: AppEvent) {
		subscribers.forEach { subscriber ->
			if (!subscriber.isEnabled) return@forEach

			runCatching {
				subscriber.onEvent(event)
			}.onFailure { throwable ->
				logger.w(throwable) { "Event subscriber '${subscriber.id}' failed." }
			}
		}
	}
}
