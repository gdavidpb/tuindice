package com.gdavidpb.tuindice.chatcontract.memory

/**
 * The names of the memory states and categories the server sends in [MemoryItem]. They are the names of academiccore's
 * MemoryState and MemoryCategory enums (domain/model/memory): the clients do not depend on academiccore, so the names
 * are written here once, and every client reads them from here.
 */
public object MemoryVocabulary {
	public const val STATE_ACTIVE: String = "ACTIVE"
	public const val STATE_PENDING_CONFIRMATION: String = "PENDING_CONFIRMATION"

	public const val CATEGORY_PREFERENCE: String = "PREFERENCE"
	public const val CATEGORY_INTEREST: String = "INTEREST"
	public const val CATEGORY_CAREER_GOAL: String = "CAREER_GOAL"
	public const val CATEGORY_TERM: String = "TERM"
	public const val CATEGORY_SUBJECT: String = "SUBJECT"
	public const val CATEGORY_DATED_RESTRICTION: String = "DATED_RESTRICTION"
}
