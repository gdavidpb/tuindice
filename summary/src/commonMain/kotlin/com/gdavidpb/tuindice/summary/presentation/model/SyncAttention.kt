package com.gdavidpb.tuindice.summary.presentation.model

// Whether the sync row asks something of the user. A problem is something to fix or wait out: it
// takes the error tint and pulses a halo until the user opens its details. What the university
// reports about the enrollment or the record is not a problem of the sync, and the screens that own
// it explain it (Record, Evaluations, Summary's own new-student screen): the row stays quiet.
enum class SyncAttention {
	None,
	Problem
}
