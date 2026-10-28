package com.gdavidpb.tuindice.assistant.presentation.testing

import com.gdavidpb.tuindice.assistant.domain.model.AssistantConversation
import com.gdavidpb.tuindice.assistant.domain.usecase.GetAssistantAcademicCalendarUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ObserveAssistantConversationUseCase
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantComponentSheet
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantComponentSheetMachine
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantComponentSheetViewModel
import com.gdavidpb.tuindice.assistant.testing.FakeAssistantAcademicCalendarRepository
import com.gdavidpb.tuindice.assistant.testing.FixedClock
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.withUnconfinedTestDispatchers
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

// The sheet over the conversation store of the test, with the real use case and machine in between.
internal class AssistantComponentSheetFixture(initial: AssistantConversation = emptyAssistantConversation()) {
	val conversation = ObservedAssistantConversationRepository(initial)
	val effects = mutableListOf<AssistantComponentSheet.Effect>()

	private val reporting = RecordingReportingRepository()

	val machine: AssistantComponentSheetMachine by lazy {
		AssistantComponentSheetMachine(
			observeAssistantConversationUseCase = ObserveAssistantConversationUseCase(conversation, reporting),
			getAssistantAcademicCalendarUseCase = GetAssistantAcademicCalendarUseCase(
				calendarRepository = FakeAssistantAcademicCalendarRepository(),
				reportingRepository = reporting
			),
			clock = FixedClock(nowMillis = 0L)
		)
	}

	fun viewModel(dispatchers: TuIndiceDispatchers): AssistantComponentSheetViewModel {
		return AssistantComponentSheetViewModel(
			screenMachine = machine,
			eventPublisher = NoOpEventPublisher,
			dispatchers = dispatchers
		)
	}
}

// Runs [block] with a view model on the test dispatchers, whose state and effects are collected from the start.
@OptIn(ExperimentalCoroutinesApi::class)
internal fun componentSheetTest(
	initial: AssistantConversation = emptyAssistantConversation(),
	block: suspend TestScope.(AssistantComponentSheetFixture, AssistantComponentSheetViewModel) -> Unit
) = runTest {
	withUnconfinedTestDispatchers { dispatchers ->
		val fixture = AssistantComponentSheetFixture(initial = initial)
		val viewModel = fixture.viewModel(dispatchers)

		backgroundScope.launchStateCollector(viewModel.state, testScheduler)
		backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
			viewModel.effect.collect { effect -> fixture.effects += effect }
		}

		block(fixture, viewModel)
	}
}

internal fun AssistantComponentSheetViewModel.openAction(messageId: String, componentId: String) =
	dispatch(AssistantComponentSheet.Action.Open(messageId = messageId, componentId = componentId))
