package com.gdavidpb.tuindice.base.data.source.event

import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.gdavidpb.tuindice.base.domain.repository.UsageDataConsentRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class BufferedEventPublisher(
	private val usageDataConsentRepository: UsageDataConsentRepository,
	private val eventSubscriber: EventSubscriber,
	bufferCapacity: Int = DEFAULT_BUFFER_CAPACITY,
	coroutineScope: CoroutineScope
) : EventPublisher {
	private val events = Channel<AppEvent>(
		capacity = bufferCapacity,
		onBufferOverflow = BufferOverflow.DROP_OLDEST
	)

	init {
		coroutineScope.launch {
			for (event in events) {
				if (!usageDataConsentRepository.isUsageDataCollectionEnabled()) continue

				runCatching {
					eventSubscriber.onEvent(event)
				}.onFailure { throwable ->
					if (throwable is CancellationException) throw throwable
				}
			}
		}
	}

	override fun publish(event: AppEvent) {
		if (!usageDataConsentRepository.isUsageDataCollectionEnabled()) return

		events.trySend(event)
	}
}

private const val DEFAULT_BUFFER_CAPACITY = 64
