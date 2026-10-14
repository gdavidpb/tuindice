package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryDestination
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryForm
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryNavDependencies
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryScreenLinks
import kotlinx.datetime.LocalDate

/**
 * Where Summary leads, connected to this navigator. The hub's jumps between tabs do not write the section the app opens
 * on (that is the bar's), so "back" from there returns to where the app started. A jump to a tab first leaves the entry
 * on top of the current tab, which is the Statistics screen when it was pushed from the hub, and then goes there in one
 * change of the stack. [onNavigateToEvaluationsWeek] and [onNavigateToRecordAttempt] are the host's: they leave the
 * request for the tab to apply and then move there, which is not the navigator's to do alone. The forms that add an
 * evaluation and plan a term are opened here, over their tab.
 */
internal fun TuIndiceNavigator.toSummaryNavDependencies(
	onNavigateToEvaluationsWeek: (weekStart: LocalDate, evaluationId: String) -> Unit,
	onNavigateToRecordAttempt: (attemptId: String) -> Unit
) = SummaryNavDependencies(
	onNavigateToUpdatePassword = { push(AuthDestination.UpdatePasswordDialog) },
	screens = SummaryScreenLinks(
		onNavigateToSection = { section -> popThenSwitchTab(section) },
		onNavigateToEvaluationsWeek = onNavigateToEvaluationsWeek,
		onNavigateToRecordAttempt = onNavigateToRecordAttempt,
		// The Statistics screen is pushed over the hub: its back returns to the Summary tab.
		onNavigateToStatistics = { push(SummaryDestination.Statistics) }
	),
	onOpenSchedule = { openSchedule() },
	onOpenForm = { form ->
		when (form) {
			SummaryForm.AddEvaluation -> showAddEvaluation()
			SummaryForm.CreateTerm -> showCreateTerm()
		}
	}
)
