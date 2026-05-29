package com.gdavidpb.tuindice.base.data.source.event

import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher

object NoOpEventPublisher : EventPublisher {
	override fun publish(event: AppEvent) = Unit
}
