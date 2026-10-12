package com.gdavidpb.tuindice.base.domain.model

/**
 * The notice owed to the user about a session that was reset behind their back. The [stamp] is the
 * identity of this notice (unique per notice, generated when it is persisted): whoever shows it takes
 * it back by its stamp, so it never takes a newer notice that was persisted in the meantime.
 */
data class SessionResetNotice(
	val reason: SessionResetNoticeReason,
	val stamp: Long
)
