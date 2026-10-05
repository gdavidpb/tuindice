package com.gdavidpb.tuindice.summary.presentation.model

// How much the sync row asks of the user. A problem is something to fix or wait out (error tint and
// halo); information is something the university reports that is not a failure of the app (neutral
// tint, no halo).
enum class SyncAttention {
	None,
	Problem,
	Informative
}
