package com.gdavidpb.tuindice.base.data.source.event

import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.launch

class BufferedEventPublisher(
	private val usageDataConsentRepository: UsageDataConsentRepository,
	private val eventSubscriber: EventSubscriber,
	bufferCapacity: Int = DEFAULT_BUFFER_CAPACITY,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers,
	private val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatchers.default)
) : EventPublisher {
	private val events = Channel<AppEvent>(
		capacity = bufferCapacity,
		onBufferOverflow = BufferOverflow.DROP_OLDEST
	)

	init {
		coroutineScope.launch {
			for (event in events) {
				if (!usageDataConsentRepository.isUsageDataCollectionEnabled()) continue

				eventSubscriber.onEvent(event)
			}
		}
	}

	override fun publish(event: AppEvent) {
		if (!usageDataConsentRepository.isUsageDataCollectionEnabled()) return

		events.trySend(event)
	}
}

private const val DEFAULT_BUFFER_CAPACITY = 64
