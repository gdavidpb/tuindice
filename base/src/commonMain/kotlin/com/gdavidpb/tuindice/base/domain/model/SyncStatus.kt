package com.gdavidpb.tuindice.base.domain.model

enum class SyncStatus {
	Healthy,
	Unavailable,
	Failed,
	OutdatedCredentials,

	// The session is valid but the university password stored on this device can no longer be
	// read (an invalidated keystore key, cleared secure storage). Unlike OutdatedCredentials the
	// password itself may still be right, so pending changes keep flushing; only the sync, which
	// needs the password, waits for the user to type it again.
	MissingCredentials,

	// DST has no academic record for this account yet (probably a new student): 424 with
	// NEW_STUDENT_NO_RECORD. It is not an outage, so it keeps the daily cooldown and arms no retry.
	NewStudentNoRecord,

	// DST denies reading a record this account does have stored: 503 with DST_RECORD_ACCESS_DENIED.
	// The previous data stays on screen.
	RecordAccessDenied;

	// Both latches are cleared the same way: the user types the password again.
	val requiresPassword: Boolean
		get() = this == OutdatedCredentials || this == MissingCredentials
}
