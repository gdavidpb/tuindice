package com.gdavidpb.tuindice.base.domain.model

/**
 * Why the session was reset behind the user's back, kept with the notice that is still owed to them
 * so that, if the process dies before the interface shows it, the next start tells the truth about
 * what happened and not about whatever happened to leave the flag behind.
 */
enum class SessionResetNoticeReason {
	/** The server ended the session (its rejection, seen while the app was running). */
	ServerEndedSession,

	/** The start-up could not read the stored session or data and reset them. */
	UnreadableData
}
