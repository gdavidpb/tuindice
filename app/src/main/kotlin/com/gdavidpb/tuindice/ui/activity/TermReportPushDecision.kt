package com.gdavidpb.tuindice.ui.activity

// Whether the intent the activity is created with carries a term report push to open. A recreated activity has read its
// intent when it was first created, and an intent the system brings back from history was delivered before: neither
// opens the report again.
internal fun shouldOpenTermReportOnCreate(
	isRestored: Boolean,
	isLaunchedFromHistory: Boolean
): Boolean = !isRestored && !isLaunchedFromHistory
