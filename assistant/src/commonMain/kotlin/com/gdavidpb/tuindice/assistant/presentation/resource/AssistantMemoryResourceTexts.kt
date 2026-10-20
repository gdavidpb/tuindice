package com.gdavidpb.tuindice.assistant.presentation.resource

import org.jetbrains.compose.resources.getString
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_memory_answer_failed
import tuindice.assistant.generated.resources.assistant_memory_cleared
import tuindice.assistant.generated.resources.assistant_memory_delete_failed
import tuindice.assistant.generated.resources.assistant_memory_deleted
import tuindice.assistant.generated.resources.assistant_memory_load_failed

// The memory texts read from the module's compose resources: the module's binding of AssistantMemoryTexts.
class AssistantMemoryResourceTexts : AssistantMemoryTexts {
	override suspend fun loadFailed(): String = getString(Res.string.assistant_memory_load_failed)

	override suspend fun deleted(): String = getString(Res.string.assistant_memory_deleted)

	override suspend fun cleared(): String = getString(Res.string.assistant_memory_cleared)

	override suspend fun deleteFailed(): String = getString(Res.string.assistant_memory_delete_failed)

	override suspend fun answerFailed(): String = getString(Res.string.assistant_memory_answer_failed)
}
