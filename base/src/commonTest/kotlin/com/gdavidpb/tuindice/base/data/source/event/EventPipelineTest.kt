package com.gdavidpb.tuindice.base.data.source.event

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.event.AppEvent
import com.gdavidpb.tuindice.base.domain.model.event.EventNames
import com.gdavidpb.tuindice.base.domain.model.event.EventParameterKeys
import com.gdavidpb.tuindice.base.data.source.usage.InMemoryUsageDataConsentRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.EventSubscriber
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
	fun bufferedEventPublisher_doesNotWriteBreadcrumbsWithoutConsent() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val publisher = BufferedEventPublisher(
			usageDataConsentRepository = InMemoryUsageDataConsentRepository(initialValue = false),
			eventSubscriber = ReportingBreadcrumbEventSubscriber(reportingRepository),
			coroutineScope = createPublisherScope()
		)

		publisher.publish(
			AppEvent.Action(
				source = "summary",
				action = "RefreshSummary"
			)
		)

		assertTrue(reportingRepository.loggedMessages.isEmpty())
		assertTrue(reportingRepository.customKeys.isEmpty())
	}

	@Test
	fun bufferedEventPublisher_writesBreadcrumbsWithConsent() = runTest {
		val reportingRepository = RecordingReportingRepository()
		val publisher = BufferedEventPublisher(
			usageDataConsentRepository = InMemoryUsageDataConsentRepository(initialValue = true),
			eventSubscriber = ReportingBreadcrumbEventSubscriber(reportingRepository),
			coroutineScope = createPublisherScope()
		)

		publisher.publish(
			AppEvent.Action(
				source = "summary",
				action = "RefreshSummary"
			)
		)

		assertEquals(
			listOf("mvi app_action source=summary action=RefreshSummary"),
			reportingRepository.loggedMessages
		)
		assertEquals("summary", reportingRepository.customKeys["mvi.current_screen"])
		assertEquals("RefreshSummary", reportingRepository.customKeys["mvi.last_action"])
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

private class EventTestMachine : ScreenMachine<EventTestState, EventTestEffect> {
	override fun initialState(): EventTestState = EventTestState.Idle

	override fun define(
		host: MachineHost<EventTestEffect>
	): MachineDefinition<EventTestState> {
		return MachineDefinition.define {
			from<EventTestState.Idle> {
				onTo<EventTestAction.Start, EventTestState.Done>(
					emits = setOf(EventTestEffect.Done::class)
				) { _, _ ->
					host.sendEffect(EventTestEffect.Done)
					EventTestState.Done
				}
			}
		}
	}
}

private class EventTestViewModel(
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers
) : StateMachineViewModel<EventTestState, EventTestAction, EventTestEffect>(
	name = "event_test",
	initialState = EventTestState.Idle,
	dispatchers = dispatchers
) {
	override val screenMachine: ScreenMachine<EventTestState, EventTestEffect> =
		EventTestMachine()

	fun startAction() {
		sendAction(EventTestAction.Start)
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
