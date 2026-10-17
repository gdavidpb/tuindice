package com.gdavidpb.tuindice.assistant.presentation.mapper

// What a memory is about, as the screen names it. Each name is a MemoryCategory of academiccore (see
// AssistantMemoryVocabulary); a category this build does not know is OTHER.
enum class AssistantMemoryCategoryKey {
	PREFERENCE,
	INTEREST,
	CAREER_GOAL,
	TERM,
	SUBJECT,
	DATED_RESTRICTION,
	OTHER;

	companion object {
		fun fromWire(category: String): AssistantMemoryCategoryKey {
			return entries.firstOrNull { key -> key.name == category } ?: OTHER
		}
	}
}
