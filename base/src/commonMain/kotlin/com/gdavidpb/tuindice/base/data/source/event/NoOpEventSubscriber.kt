package com.gdavidpb.tuindice.base.data.source.event

import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber

object NoOpEventSubscriber : EventSubscriber {
	override val id: String = "noop"
	override val isEnabled: Boolean = false

	override fun onEvent(event: AppEvent) = Unit
}
