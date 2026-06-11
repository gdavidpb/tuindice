package com.gdavidpb.tuindice.base.data.source.event

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.model.event.EventNames
import com.gdavidpb.tuindice.base.domain.model.event.EventParameterKeys
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout

class EventPipelineTest {
	@Test
	fun appEvent_actionUsesClosedDefaultParameters() {
		val event = AppEvent.Action(
			source = "summary",
			action = "refresh_summary"
		)

		assertEquals(
			mapOf(
				EventParameterKeys.SOURCE to "summary",
				EventParameterKeys.ACTION to "refresh_summary"
			),
			event.parameters
		)
	}

	@Test
	fun appEvent_screenViewUsesClosedDefaultParameters() {
		val event = AppEvent.ScreenView(source = "summary")

		assertEquals(EventNames.SCREEN_VIEW, event.name)
		assertEquals(
			mapOf(
				EventParameterKeys.SOURCE to "summary",
				EventParameterKeys.SCREEN_NAME to "summary"
			),
			event.parameters
		)
	}

	@Test
	fun bufferedEventPublisher_doesNotSendWithoutConsent() = runTest {
		val subscriber = RecordingEventSubscriber()
		val publisher = BufferedEventPublisher(
			usageDataConsentRepository = InMemoryUsageDataConsentRepository(initialValue = false),
			eventSubscriber = subscriber,
			coroutineScope = createPublisherScope()
		)

		publisher.publish(
			AppEvent.Action(
				source = "summary",
				action = "refresh_summary"
			)
		)

		assertTrue(subscriber.events.isEmpty())
	}

	@Test
	fun bufferedEventPublisher_sendsWithConsent() = runTest {
		val subscriber = RecordingEventSubscriber()
		val publisher = BufferedEventPublisher(
			usageDataConsentRepository = InMemoryUsageDataConsentRepository(initialValue = true),
			eventSubscriber = subscriber,
			coroutineScope = createPublisherScope()
		)
		val event = AppEvent.Action(
			source = "summary",
			action = "refresh_summary"
		)

		publisher.publish(event)

		assertEquals(listOf<AppEvent>(event), subscriber.events)
	}

	@Test
	fun compositeEventSubscriber_isolatesSubscriberErrors() {
		val recordingSubscriber = RecordingEventSubscriber()
		val composite = CompositeEventSubscriber(
			subscribers = listOf(
				ThrowingEventSubscriber(),
				recordingSubscriber
			)
		)

		val event = AppEvent.Action(
			source = "summary",
			action = "refresh_summary"
		)
		composite.onEvent(event)

		assertEquals(listOf<AppEvent>(event), recordingSubscriber.events)
	}

	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun baseViewModel_publishesAutomaticEvents() = runTest {
		withMainDispatcher { dispatchers ->
			val recordingEventPublisher = RecordingEventPublisher()
			val viewModel = EventTestViewModel(
				eventPublisher = recordingEventPublisher,
				dispatchers = dispatchers
			)

			val stateJob = backgroundScope.launchStateCollector(
				flow = viewModel.state,
				testScheduler = testScheduler
			)

			try {
				viewModel.state.test {
					awaitItem()
					viewModel.startAction()
					awaitItem()

					withTimeout(1_000) {
						recordingEventPublisher.eventsFlow.first { events ->
							val eventNames = events.map { event -> event.name }
							EventNames.SCREEN_VIEW in eventNames &&
								EventNames.APP_ACTION in eventNames &&
								EventNames.APP_STATE in eventNames &&
								EventNames.APP_EFFECT in eventNames
						}
					}
				}
			} finally {
				stateJob.cancel()
			}

			val eventNames = recordingEventPublisher.events.map { event -> event.name }

			assertTrue(EventNames.SCREEN_VIEW in eventNames)
			assertTrue(EventNames.APP_ACTION in eventNames)
			assertTrue(EventNames.APP_STATE in eventNames)
			assertTrue(EventNames.APP_EFFECT in eventNames)
			assertTrue(recordingEventPublisher.events.all { event ->
				event.parameters[EventParameterKeys.SOURCE] == "event_test"
			})
			assertEquals(
				"event_test",
				recordingEventPublisher.events
					.first { event -> event.name == EventNames.SCREEN_VIEW }
					.parameters[EventParameterKeys.SCREEN_NAME]
			)
			assertFalse(recordingEventPublisher.events.any { event ->
				event.parameters.containsKey("password") || event.parameters.containsKey("usbId")
			})
		}
	}
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun TestScope.createPublisherScope(): CoroutineScope {
	return CoroutineScope(backgroundScope.coroutineContext + UnconfinedTestDispatcher(testScheduler))
}

private class RecordingEventSubscriber : EventSubscriber {
	val events = mutableListOf<AppEvent>()

	override val id: String = "recording"
	override val isEnabled: Boolean = true

	override fun onEvent(event: AppEvent) {
		events += event
	}
}

private class ThrowingEventSubscriber : EventSubscriber {
	override val id: String = "throwing"
	override val isEnabled: Boolean = true

	override fun onEvent(event: AppEvent) {
		error("Subscriber failed")
	}
}

private class RecordingEventPublisher : EventPublisher {
	val eventsFlow = MutableStateFlow<List<AppEvent>>(emptyList())
	val events: List<AppEvent>
		get() = eventsFlow.value

	override fun publish(event: AppEvent) {
		eventsFlow.value += event
	}
}

private class EventTestViewModel(
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers
) : BaseViewModel<EventTestState, EventTestAction, EventTestEffect>(
	name = "event_test",
	initialState = EventTestState.Idle,
	dispatchers = dispatchers
) {
	fun startAction() {
		sendAction(EventTestAction.Start)
	}

	override suspend fun processAction(
		action: EventTestAction,
		sideEffect: (EventTestEffect) -> Unit
	): Flow<Mutation<EventTestState>> {
		sideEffect(EventTestEffect.Done)
		return flowOf { EventTestState.Done }
	}
}

private sealed class EventTestState : ViewState {
	data object Idle : EventTestState()
	data object Done : EventTestState()
}

private sealed class EventTestAction : ViewAction {
	data object Start : EventTestAction()
}

private sealed class EventTestEffect : ViewEffect {
	data object Done : EventTestEffect()
}
