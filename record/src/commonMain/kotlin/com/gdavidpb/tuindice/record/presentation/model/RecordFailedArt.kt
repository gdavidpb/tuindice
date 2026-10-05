package com.gdavidpb.tuindice.record.presentation.model

// The illustration above the failed state. A university that has no record for the account yet is
// not a failure of ours, so that case gets the calm art instead of the error one.
enum class RecordFailedArt {
	Error,
	NoRecord
}
