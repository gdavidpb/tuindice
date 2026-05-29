package com.gdavidpb.tuindice.base.data.source.event

import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.AnalyticsConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.launch

class BufferedEventPublisher(
	private val analyticsConsentRepository: AnalyticsConsentRepository,
	private val eventSubscriber: EventSubscriber,
	bufferCapacity: Int = DEFAULT_BUFFER_CAPACITY
) : EventPublisher {
	private val events = Channel<AppEvent>(
		capacity = bufferCapacity,
		onBufferOverflow = BufferOverflow.DROP_OLDEST
	)
	private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

	init {
		scope.launch {
			for (event in events) {
				if (!analyticsConsentRepository.isAnalyticsCollectionEnabled()) continue

				eventSubscriber.onEvent(event)
			}
		}
	}

	override fun publish(event: AppEvent) {
		if (!analyticsConsentRepository.isAnalyticsCollectionEnabled()) return

		events.trySend(event)
	}
}

private const val DEFAULT_BUFFER_CAPACITY = 64
