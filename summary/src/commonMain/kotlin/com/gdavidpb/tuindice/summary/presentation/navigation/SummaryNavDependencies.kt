package com.gdavidpb.tuindice.summary.presentation.navigation

import androidx.compose.runtime.Stable
import com.gdavidpb.tuindice.base.domain.model.MainSection
import kotlinx.datetime.LocalDate

/**
 * Where Summary leads: the lines of the hub and the sync status icon, all of them screens Summary
 * does not know. The host connects each one, and until then tapping does nothing.
 */
@Stable
class SummaryNavDependencies(
	// The sheet that asks the student to enter the password again (the sync status icon leads there).
	val onNavigateToUpdatePassword: () -> Unit = {},
	// The tab of the section: the Record tab (the term, and the enrollment errors), the Pensum tab (the career) and
	// the Evaluations tab as it opens by itself, on no week (the Statistics screen leads there from its link).
	val onNavigateToSection: (section: MainSection) -> Unit = {},
	// The Evaluations tab on the week that this Monday opens, with that evaluation brought into view.
	val onNavigateToEvaluationsWeek: (weekStart: LocalDate, evaluationId: String) -> Unit = { _, _ -> },
	// The Record tab on the term of that attempt, with its card brought into view.
	val onNavigateToRecordAttempt: (attemptId: String) -> Unit = {},
	// The Schedule sheet, over Summary.
	val onOpenSchedule: () -> Unit = {},
	// The form, on the tab it belongs to.
	val onOpenForm: (form: SummaryForm) -> Unit = {}
)
