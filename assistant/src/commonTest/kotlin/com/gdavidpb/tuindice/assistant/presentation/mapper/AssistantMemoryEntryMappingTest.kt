package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.assistant.presentation.testing.MEMORY_NOW_MILLIS
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.chatcontract.memory.MemoryItem
import com.gdavidpb.tuindice.chatcontract.memory.MemoryVocabulary
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_memory_since
import kotlin.test.Test
import kotlin.test.assertEquals

// The rows the memory screen lists, as the mapper makes them: which memories are listed, the category each one keeps
// and the day it was saved, told against the moment the list is read.
class AssistantMemoryEntryMappingTest {
	@Test
	fun when_aMemoryWasSavedThisYear_then_itsSinceTextLeavesOutTheYear() {
		val createdAt = millisOf(year = 2026, month = 10, day = 9)

		val entry = listOf(memory(createdAtMillis = createdAt))
			.toMemoryEntryItems(nowMillis = MEMORY_NOW_MILLIS)
			.single()

		assertEquals(
			UiText.Resource(
				Res.string.assistant_memory_since,
				listOf(createdAt.formatDate(DateTextStyle.DAY_MONTH))
			),
			entry.sinceText
		)
	}

	@Test
	fun when_aMemoryWasSavedInAnotherYear_then_itsSinceTextNamesTheYear() {
		val createdAt = millisOf(year = 2025, month = 10, day = 9)

		val entry = listOf(memory(createdAtMillis = createdAt))
			.toMemoryEntryItems(nowMillis = MEMORY_NOW_MILLIS)
			.single()

		assertEquals(
			UiText.Resource(
				Res.string.assistant_memory_since,
				listOf(createdAt.formatDate(DateTextStyle.DAY_MONTH_YEAR_UNPADDED))
			),
			entry.sinceText
		)
	}

	@Test
	fun when_aMemoryIsArchived_then_itIsNotListed() {
		val entries = listOf(
			memory(id = "m1", state = MemoryVocabulary.STATE_ACTIVE),
			memory(id = "m2", state = "ARCHIVED")
		).toMemoryEntryItems(nowMillis = MEMORY_NOW_MILLIS)

		assertEquals(listOf("m1"), entries.map { entry -> entry.id })
	}

	@Test
	fun when_aMemoryIsPendingConfirmation_then_itAsksForIt() {
		val entries = listOf(
			memory(id = "m1", state = MemoryVocabulary.STATE_ACTIVE),
			memory(id = "m2", state = MemoryVocabulary.STATE_PENDING_CONFIRMATION)
		).toMemoryEntryItems(nowMillis = MEMORY_NOW_MILLIS)

		assertEquals(listOf(false, true), entries.map { entry -> entry.needsConfirmation })
	}

	@Test
	fun when_aCategoryIsUnknown_then_itIsOther() {
		val entries = listOf(
			memory(id = "m1", category = MemoryVocabulary.CATEGORY_CAREER_GOAL),
			memory(id = "m2", category = "A_KIND_THIS_BUILD_DOES_NOT_KNOW")
		).toMemoryEntryItems(nowMillis = MEMORY_NOW_MILLIS)

		assertEquals(
			listOf(AssistantMemoryCategoryKey.CAREER_GOAL, AssistantMemoryCategoryKey.OTHER),
			entries.map { entry -> entry.category }
		)
	}

	private fun memory(
		id: String = "m1",
		category: String = MemoryVocabulary.CATEGORY_PREFERENCE,
		state: String = MemoryVocabulary.STATE_ACTIVE,
		createdAtMillis: Long = millisOf(year = 2026, month = 10, day = 9)
	): MemoryItem =
		MemoryItem(
			id = id,
			category = category,
			text = "Prefiero clases por la mañana",
			state = state,
			anchorLabel = null,
			createdAtMillis = createdAtMillis
		)

	private fun millisOf(year: Int, month: Int, day: Int): Long =
		LocalDateTime(year, month, day, 12, 0)
			.toInstant(TimeZone.currentSystemDefault())
			.toEpochMilliseconds()
}
