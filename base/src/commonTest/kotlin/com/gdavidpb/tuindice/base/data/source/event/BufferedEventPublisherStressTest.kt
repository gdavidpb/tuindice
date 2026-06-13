package com.gdavidpb.tuindice.base.data.source.event

import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.model.event.EventParameterKeys
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Multi-threaded stress over the real pipeline: unlike the deterministic unconfined
 * tests in [EventPipelineTest], these run producers and the consumer loop on
 * Dispatchers.Default to exercise actual concurrency. Draining is deterministic via a
 * sentinel event (channel FIFO guarantees every surviving earlier event arrives first);
 * the sentinel is re-published on timeout because DROP_OLDEST may drop it under load.
 * Timeouts run inside withContext(Dispatchers.Default) so they use real time, not
 * runTest's virtual clock.
 */
class BufferedEventPublisherStressTest {
	@Test
	fun publish_underConcurrentProducers_neverCrashes_andKeepsPerProducerFifo() = runTest {
		withContext(Dispatchers.Default) {
			val consentRepository = InMemoryUsageDataConsentRepository(initialValue = true)
			val received = Channel<AppEvent>(capacity = Channel.UNLIMITED)
			val publisherScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
			val publisher = BufferedEventPublisher(
				usageDataConsentRepository = consentRepository,
				eventSubscriber = ChannelEventSubscriber(received),
				coroutineScope = publisherScope
			)

			try {
				val producerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
				val producers = (0 until PRODUCERS).map { producer ->
					producerScope.launch {
						repeat(EVENTS_PER_PRODUCER) { index ->
							publisher.publish(
								AppEvent.Action(source = "stress_$producer", action = "event_$index")
							)
						}
					}
				}
				producers.joinAll()
				producerScope.cancel()

				val delivered = drainUntilSentinel(publisher, received)

				assertTrue(
					delivered.size <= PRODUCERS * EVENTS_PER_PRODUCER,
					"Delivered ${delivered.size} events, more than the ${PRODUCERS * EVENTS_PER_PRODUCER} published"
				)

				delivered
					.groupBy { event -> event.parameters[EventParameterKeys.SOURCE] }
					.forEach { (source, events) ->
						val indices = events.map { event ->
							val action = event.parameters[EventParameterKeys.ACTION]
								?: fail("Delivered event without action parameter")
							action.removePrefix("event_").toInt()
						}
						assertTrue(
							indices.zipWithNext().all { (previous, next) -> previous < next },
							"Delivery for $source must be strictly increasing (per-producer FIFO, no duplicates): $indices"
						)
					}
			} finally {
				publisherScope.cancel()
			}
		}
	}

	@Test
	fun consentToggledConcurrently_neverCrashes_andRevokedPublishesAreDropped() = runTest {
		withContext(Dispatchers.Default) {
			val consentRepository = InMemoryUsageDataConsentRepository(initialValue = true)
			val received = Channel<AppEvent>(capacity = Channel.UNLIMITED)
			val publisherScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
			val publisher = BufferedEventPublisher(
				usageDataConsentRepository = consentRepository,
				eventSubscriber = ChannelEventSubscriber(received),
				coroutineScope = publisherScope
			)

			try {
				val stormScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
				val storm = (0 until PRODUCERS).map { producer ->
					stormScope.launch {
						repeat(EVENTS_PER_PRODUCER) { index ->
							publisher.publish(
								AppEvent.Action(source = "storm_$producer", action = "event_$index")
							)
						}
					}
				} + stormScope.launch {
					repeat(CONSENT_TOGGLES) { toggle ->
						consentRepository.setUsageDataCollectionEnabled(toggle % 2 == 0)
					}
				}
				storm.joinAll()
				stormScope.cancel()

				// Surviving the storm without crashing is the core assertion; now check
				// the revoked-consent gate deterministically.
				consentRepository.setUsageDataCollectionEnabled(true)
				drainUntilSentinel(publisher, received)

				consentRepository.setUsageDataCollectionEnabled(false)
				repeat(REVOKED_PUBLISHES) { index ->
					publisher.publish(AppEvent.Action(source = "revoked", action = "event_$index"))
				}

				consentRepository.setUsageDataCollectionEnabled(true)
				val afterRevoke = drainUntilSentinel(publisher, received)

				val leaked = afterRevoke.filter { event ->
					event.parameters[EventParameterKeys.SOURCE] == "revoked"
				}
				assertTrue(
					leaked.isEmpty(),
					"Events published while consent was revoked must be dropped, leaked: ${leaked.size}"
				)
			} finally {
				publisherScope.cancel()
			}
		}
	}

	private suspend fun drainUntilSentinel(
		publisher: BufferedEventPublisher,
		received: Channel<AppEvent>
	): List<AppEvent> {
		val delivered = mutableListOf<AppEvent>()
		var attempts = 0

		publisher.publish(SENTINEL)
		while (true) {
			val event = withTimeoutOrNull(2_000) { received.receive() }
			if (event == null) {
				attempts++
				if (attempts > MAX_SENTINEL_ATTEMPTS) {
					fail("Sentinel was never delivered after $attempts attempts")
				}
				publisher.publish(SENTINEL)
				continue
			}
			if (event.parameters[EventParameterKeys.SOURCE] == SENTINEL_SOURCE) {
				return delivered
			}
			delivered += event
		}
	}
}

private class ChannelEventSubscriber(
	private val received: Channel<AppEvent>
) : EventSubscriber {
	override val id: String = "stress-recording"
	override val isEnabled: Boolean = true

	override fun onEvent(event: AppEvent) {
		received.trySend(event)
	}
}

private const val PRODUCERS = 8
private const val EVENTS_PER_PRODUCER = 200
private const val CONSENT_TOGGLES = 500
private const val REVOKED_PUBLISHES = 50
private const val MAX_SENTINEL_ATTEMPTS = 5
private const val SENTINEL_SOURCE = "stress_sentinel"
private val SENTINEL = AppEvent.Action(source = SENTINEL_SOURCE, action = "sentinel")
