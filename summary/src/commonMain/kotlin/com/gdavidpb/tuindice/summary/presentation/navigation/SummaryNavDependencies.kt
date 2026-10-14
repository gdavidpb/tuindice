package com.gdavidpb.tuindice.summary.presentation.navigation

import androidx.compose.runtime.Stable
import kotlinx.datetime.LocalDate

/**
 * Where Summary leads: the lines of the hub and the sync status icon, all of them screens Summary
 * does not know. The host connects each one, and until then tapping does nothing.
 */
@Stable
class SummaryNavDependencies(
	// The sheet that asks the student to enter the password again (the sync status icon leads there).
	val onNavigateToUpdatePassword: () -> Unit = {},
	// The Record tab (the term, and the enrollment errors).
	val onNavigateToRecord: () -> Unit = {},
	// The Pensum tab (the career).
	val onNavigateToPensum: () -> Unit = {},
	// The Evaluations tab on the week that this Monday opens, with that evaluation brought into view.
	val onNavigateToEvaluationsWeek: (weekStart: LocalDate, evaluationId: String) -> Unit = { _, _ -> },
	// The Record tab on the term of that attempt, with its card brought into view.
	val onNavigateToRecordAttempt: (attemptId: String) -> Unit = {},
	// The Schedule sheet, over Summary.
	val onOpenSchedule: () -> Unit = {}
)
