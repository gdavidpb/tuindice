package com.gdavidpb.tuindice.presentation.navigation

import com.gdavidpb.tuindice.auth.presentation.navigation.AuthDestination
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.summary.presentation.navigation.SummaryNavDependencies
import kotlinx.datetime.LocalDate

/**
 * Where Summary leads, connected to this navigator. The hub's jumps between tabs do not write the
 * section the app opens on (that is the bar's), so "back" from there returns to where the app
 * started. [onNavigateToEvaluationsWeek] and [onNavigateToRecordAttempt] are the host's: they leave the
 * request for the tab to apply and then move there, which is not the navigator's to do alone.
 */
internal fun TuIndiceNavigator.toSummaryNavDependencies(
	onNavigateToEvaluationsWeek: (weekStart: LocalDate, evaluationId: String) -> Unit,
	onNavigateToRecordAttempt: (attemptId: String) -> Unit
) = SummaryNavDependencies(
	onNavigateToUpdatePassword = { push(AuthDestination.UpdatePasswordDialog) },
	onNavigateToRecord = { switchTab(MainSection.RECORD) },
	onNavigateToPensum = { switchTab(MainSection.PENSUM) },
	onNavigateToEvaluationsWeek = onNavigateToEvaluationsWeek,
	onNavigateToRecordAttempt = onNavigateToRecordAttempt,
	onOpenSchedule = ::openSchedule
)
