package com.gdavidpb.tuindice.summary.presentation.navigation

import androidx.compose.runtime.Stable
import com.gdavidpb.tuindice.base.domain.model.MainSection
import kotlinx.datetime.LocalDate

/**
 * The screens the lines of the hub lead to, each one connected by the host: a tab of a section, the Evaluations tab on
 * a week, the Record tab on an attempt, the Statistics screen and the sheet that shares a closed term, both pushed over
 * the hub. Until the host connects one, tapping the line that leads there does nothing.
 */
@Stable
class SummaryScreenLinks(
	// The tab of the section: the Record tab (the term, and the enrollment errors), the Pensum tab (the career) and
	// the Evaluations tab as it opens by itself, on no week (the Statistics screen leads there from its link).
	val onNavigateToSection: (section: MainSection) -> Unit = {},
	// The Evaluations tab on the week that this Monday opens, with that evaluation brought into view.
	val onNavigateToEvaluationsWeek: (weekStart: LocalDate, evaluationId: String) -> Unit = { _, _ -> },
	// The Record tab on the term of that attempt, with its card brought into view.
	val onNavigateToRecordAttempt: (attemptId: String) -> Unit = {},
	// The Statistics screen, pushed over the hub.
	val onNavigateToStatistics: () -> Unit = {},
	// The sheet that shares a closed term as an image, pushed over the hub, opened on the term of that key.
	val onNavigateToShareTerm: (termKey: String) -> Unit = {},
	// The term report of the insight, pushed over the hub: the line of the report leads there.
	val onNavigateToTermReport: (insightId: String) -> Unit = {}
)
