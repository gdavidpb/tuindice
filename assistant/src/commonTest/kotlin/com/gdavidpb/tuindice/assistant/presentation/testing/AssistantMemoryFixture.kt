package com.gdavidpb.tuindice.assistant.presentation.testing

import com.gdavidpb.tuindice.assistant.domain.repository.AssistantMemoryRepository
import com.gdavidpb.tuindice.assistant.domain.usecase.AnswerStillAppliesUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.ClearAssistantMemoryUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.DeleteAssistantMemoryItemUseCase
import com.gdavidpb.tuindice.assistant.domain.usecase.GetAssistantMemoryUseCase
import com.gdavidpb.tuindice.assistant.presentation.contract.AssistantMemory
import com.gdavidpb.tuindice.assistant.presentation.machine.AssistantMemoryMachine
import com.gdavidpb.tuindice.assistant.presentation.resource.AssistantMemoryTexts
import com.gdavidpb.tuindice.assistant.presentation.viewmodel.AssistantMemoryViewModel
import com.gdavidpb.tuindice.assistant.testing.FixedClock
import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.chatcontract.memory.MemoryItem
import com.gdavidpb.tuindice.chatcontract.memory.MemoryList
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.withUnconfinedTestDispatchers
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

// The moment the memory screen is read in the tests: 20 October 2026, at noon in UTC.
internal const val MEMORY_NOW_MILLIS = 1_792_497_600_000L

// The texts are told apart by their values: each answers its own key, so an assertion can say which text the student
// sees.
internal class FakeAssistantMemoryTexts : AssistantMemoryTexts {
	override suspend fun loadFailed(): String = "load-failed"

	override suspend fun deleted(): String = "deleted"

	override suspend fun cleared(): String = "cleared"

	override suspend fun deleteFailed(): String = "delete-failed"

	override suspend fun answerFailed(): String = "answer-failed"
}

// The memory as the server keeps it: the list it answers, the writes it takes, and the failures a test asks for.
internal class ServerAssistantMemoryRepository(items: List<MemoryItem> = emptyList()) : AssistantMemoryRepository {
	var items: List<MemoryItem> = items
	var listFails = false
	var deleteFails = false
	var answerFails = false
	var clearFails = false

	// The delete waits for the test's go-ahead while the gate is set: the request stays on its way until it is completed.
	var deleteGate: CompletableDeferred<Unit>? = null
	val deleted = mutableListOf<String>()
	val answers = mutableListOf<Pair<String, Boolean>>()
	var clears = 0

	override suspend fun listMemories(): MemoryList {
		check(!listFails) { "list failed" }

		return MemoryList(items = items)
	}

	override suspend fun deleteMemory(memoryId: String) {
		deleteGate?.await()
		check(!deleteFails) { "delete failed" }
		// A memory the server no longer keeps is not there to delete, so a second delete of it fails.
		check(items.any { item -> item.id == memoryId }) { "memory not found" }

		deleted += memoryId
		items = items.filterNot { item -> item.id == memoryId }
	}

	override suspend fun deleteAllMemories() {
		check(!clearFails) { "clear failed" }

		clears++
		items = emptyList()
	}

	override suspend fun answerStillApplies(memoryId: String, applies: Boolean) {
		check(!answerFails) { "answer failed" }

		answers += memoryId to applies
	}
}

// The memory screen over the fake server, with the real use cases and machine in between.
internal class AssistantMemoryFixture(items: List<MemoryItem> = emptyList()) {
	val texts = FakeAssistantMemoryTexts()
	val repository = ServerAssistantMemoryRepository(items = items)
	val effects = mutableListOf<AssistantMemory.Effect>()

	private val reporting = RecordingReportingRepository()

	val machine: AssistantMemoryMachine by lazy {
		AssistantMemoryMachine(
			getAssistantMemoryUseCase = GetAssistantMemoryUseCase(repository, reporting),
			deleteAssistantMemoryItemUseCase = DeleteAssistantMemoryItemUseCase(repository, reporting),
			clearAssistantMemoryUseCase = ClearAssistantMemoryUseCase(repository, reporting),
			answerStillAppliesUseCase = AnswerStillAppliesUseCase(repository, reporting),
			texts = texts,
			clock = FixedClock(nowMillis = MEMORY_NOW_MILLIS)
		)
	}

	fun viewModel(dispatchers: TuIndiceDispatchers): AssistantMemoryViewModel {
		return AssistantMemoryViewModel(
			screenMachine = machine,
			eventPublisher = NoOpEventPublisher,
			dispatchers = dispatchers
		)
	}

	fun snackBars(): List<String> = effects
		.filterIsInstance<AssistantMemory.Effect.ShowSnackBar>()
		.map { effect -> effect.message }
}

// Runs [block] with a view model on the test dispatchers. The store is set up by [configure] before the screen starts,
// so a failure set there is met by the first load; the state and the effects are collected from the start.
@OptIn(ExperimentalCoroutinesApi::class)
internal fun memoryTest(
	items: List<MemoryItem> = emptyList(),
	configure: ServerAssistantMemoryRepository.() -> Unit = {},
	block: suspend TestScope.(AssistantMemoryFixture, AssistantMemoryViewModel) -> Unit
) = runTest {
	withUnconfinedTestDispatchers { dispatchers ->
		val fixture = AssistantMemoryFixture(items = items)
		fixture.repository.configure()

		val viewModel = fixture.viewModel(dispatchers)

		backgroundScope.launchStateCollector(viewModel.state, testScheduler)
		backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
			viewModel.effect.collect { effect -> fixture.effects += effect }
		}

		block(fixture, viewModel)
	}
}

internal fun memoryItem(
	id: String,
	category: String = "PREFERENCE",
	state: String = "ACTIVE",
	text: String = "Prefiero clases por la mañana",
	anchorLabel: String? = null
): MemoryItem {
	return MemoryItem(
		id = id,
		category = category,
		text = text,
		state = state,
		anchorLabel = anchorLabel,
		createdAtMillis = 0L
	)
}

internal fun AssistantMemoryViewModel.content(): AssistantMemory.State.Content =
	state.value as AssistantMemory.State.Content

internal fun AssistantMemoryViewModel.deleteAction(memoryId: String) =
	dispatch(AssistantMemory.Action.DeleteEntry(memoryId = memoryId))

internal fun AssistantMemoryViewModel.answerAction(memoryId: String, applies: Boolean) =
	dispatch(AssistantMemory.Action.AnswerStillApplies(memoryId = memoryId, applies = applies))

internal fun AssistantMemoryViewModel.clearAllAction() =
	dispatch(AssistantMemory.Action.ClearAll)

internal fun AssistantMemoryViewModel.confirmClearAllAction() =
	dispatch(AssistantMemory.Action.ConfirmClearAll)

internal fun AssistantMemoryViewModel.retryAction() =
	dispatch(AssistantMemory.Action.Retry)
