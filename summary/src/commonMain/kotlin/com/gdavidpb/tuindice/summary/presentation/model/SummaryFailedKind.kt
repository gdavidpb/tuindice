package com.gdavidpb.tuindice.summary.presentation.model

// Which screen the Failed state paints. A new student is not a failure of the app, so it gets the
// calm illustrated message with a secondary retry instead of the error view.
enum class SummaryFailedKind {
	Error,
	NewStudentNoRecord
}
