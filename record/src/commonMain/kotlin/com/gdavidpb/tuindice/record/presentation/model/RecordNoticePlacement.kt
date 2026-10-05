package com.gdavidpb.tuindice.record.presentation.model

// Where a notice sits. What describes the current term goes on that term's page; a final annulment
// has no current term to sit on and goes above the pager, shared by every page.
enum class RecordNoticePlacement {
	CurrentTermPage,
	AbovePager
}
