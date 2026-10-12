package com.gdavidpb.tuindice.presentation.machine

/** The notice owed to the user about a session that was reset, told: its [stamp] and what it says. */
data class SessionNoticeText(
	val stamp: Long,
	val message: String
)
