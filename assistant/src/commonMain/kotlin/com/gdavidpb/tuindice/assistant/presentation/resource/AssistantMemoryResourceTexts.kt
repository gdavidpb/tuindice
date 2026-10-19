package com.gdavidpb.tuindice.assistant.presentation.resource

import com.gdavidpb.tuindice.assistant.presentation.mapper.AssistantMemoryCategoryKey
import org.jetbrains.compose.resources.getString
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_memory_answer_failed
import tuindice.assistant.generated.resources.assistant_memory_category_career_goal
import tuindice.assistant.generated.resources.assistant_memory_category_dated_restriction
import tuindice.assistant.generated.resources.assistant_memory_category_interest
import tuindice.assistant.generated.resources.assistant_memory_category_other
import tuindice.assistant.generated.resources.assistant_memory_category_preference
import tuindice.assistant.generated.resources.assistant_memory_category_subject
import tuindice.assistant.generated.resources.assistant_memory_category_term
import tuindice.assistant.generated.resources.assistant_memory_cleared
import tuindice.assistant.generated.resources.assistant_memory_delete_failed
import tuindice.assistant.generated.resources.assistant_memory_deleted
import tuindice.assistant.generated.resources.assistant_memory_load_failed

// The memory texts read from the module's compose resources: the module's binding of AssistantMemoryTexts.
class AssistantMemoryResourceTexts : AssistantMemoryTexts {
	override suspend fun categoryLabel(category: AssistantMemoryCategoryKey): String = getString(
		when (category) {
			AssistantMemoryCategoryKey.PREFERENCE -> Res.string.assistant_memory_category_preference
			AssistantMemoryCategoryKey.INTEREST -> Res.string.assistant_memory_category_interest
			AssistantMemoryCategoryKey.CAREER_GOAL -> Res.string.assistant_memory_category_career_goal
			AssistantMemoryCategoryKey.TERM -> Res.string.assistant_memory_category_term
			AssistantMemoryCategoryKey.SUBJECT -> Res.string.assistant_memory_category_subject
			AssistantMemoryCategoryKey.DATED_RESTRICTION -> Res.string.assistant_memory_category_dated_restriction
			AssistantMemoryCategoryKey.OTHER -> Res.string.assistant_memory_category_other
		}
	)

	override suspend fun loadFailed(): String = getString(Res.string.assistant_memory_load_failed)

	override suspend fun deleted(): String = getString(Res.string.assistant_memory_deleted)

	override suspend fun cleared(): String = getString(Res.string.assistant_memory_cleared)

	override suspend fun deleteFailed(): String = getString(Res.string.assistant_memory_delete_failed)

	override suspend fun answerFailed(): String = getString(Res.string.assistant_memory_answer_failed)
}
