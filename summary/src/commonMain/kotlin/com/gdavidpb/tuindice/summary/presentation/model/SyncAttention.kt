package com.gdavidpb.tuindice.summary.presentation.model

// How much the sync row asks of the user. A problem is something to fix or wait out (error tint);
// information is something the university reports that is not a failure of the app (accent tint).
// Both matter enough to pulse a halo in their tint until the user opens the details.
enum class SyncAttention {
	None,
	Problem,
	Informative
}
