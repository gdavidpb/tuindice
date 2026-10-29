package com.gdavidpb.tuindice.assistant.presentation.mapper

import com.gdavidpb.tuindice.chatcontract.memory.MemoryVocabulary

// What a memory is about, as the screen names it. Each one is a category of MemoryVocabulary; a category this build
// does not know is OTHER.
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
			return when (category) {
				MemoryVocabulary.CATEGORY_PREFERENCE -> PREFERENCE
				MemoryVocabulary.CATEGORY_INTEREST -> INTEREST
				MemoryVocabulary.CATEGORY_CAREER_GOAL -> CAREER_GOAL
				MemoryVocabulary.CATEGORY_TERM -> TERM
				MemoryVocabulary.CATEGORY_SUBJECT -> SUBJECT
				MemoryVocabulary.CATEGORY_DATED_RESTRICTION -> DATED_RESTRICTION
				else -> OTHER
			}
		}
	}
}
