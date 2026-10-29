package com.gdavidpb.tuindice.assistant.domain.usecase

import com.gdavidpb.tuindice.assistant.domain.usecase.param.AnswerStillAppliesParams
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantConversationRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantMemoryRepository
import com.gdavidpb.tuindice.assistant.testing.RecordingAssistantTurnRepository
import com.gdavidpb.tuindice.assistant.testing.dataValues
import com.gdavidpb.tuindice.chatcontract.memory.MemoryItem
import com.gdavidpb.tuindice.chatcontract.memory.MemoryList
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AssistantMemoryUseCasesTest {
	private val memory = RecordingAssistantMemoryRepository()
	private val turns = RecordingAssistantTurnRepository()
	private val reporting = RecordingReportingRepository()

	@Test
	fun theMemoryIsReadFromTheServer() = runTest {
		memory.memories = MemoryList(
			items = listOf(
				MemoryItem(
					id = "x1",
					category = "habitos",
					text = "Estudia de noche",
					state = "active",
					createdAtMillis = 1L
				)
			)
		)

		val values = GetAssistantMemoryUseCase(memory, reporting).execute(Unit).dataValues()

		assertEquals(listOf(memory.memories), values)
	}

	@Test
	fun oneItemIsDeletedByItsId() = runTest {
		DeleteAssistantMemoryItemUseCase(memory, reporting).execute("x1").dataValues()

		assertEquals(listOf("x1"), memory.deleted)
	}

	@Test
	fun clearingTheMemoryDeletesEverythingOnTheServer() = runTest {
		ClearAssistantMemoryUseCase(memory, reporting).execute(Unit).dataValues()

		assertEquals(1, memory.allDeleted)
	}

	@Test
	fun aStillAppliesAnswerIsSentForItsItem() = runTest {
		AnswerStillAppliesUseCase(memory, reporting)
			.execute(AnswerStillAppliesParams(memoryId = "x1", applies = false))
			.dataValues()

		assertEquals(listOf("x1" to false), memory.answers)
	}

	@Test
	fun deletingTheConversationsDeletesThemOnTheServerAndForgetsThemHere() = runTest {
		val conversation = RecordingAssistantConversationRepository()

		DeleteAssistantConversationsUseCase(turns, conversation, reporting).execute(Unit).dataValues()

		assertEquals(1, turns.conversationsDeleted)
		assertEquals(1, conversation.cleared)
	}
}
