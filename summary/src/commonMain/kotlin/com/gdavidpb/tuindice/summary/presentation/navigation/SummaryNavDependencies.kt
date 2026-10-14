package com.gdavidpb.tuindice.summary.presentation.navigation

import androidx.compose.runtime.Stable

/**
 * Where Summary leads: the screens its lines lead to, the Schedule sheet, the forms and the password sheet. The
 * host connects each one, and until then tapping does nothing.
 */
@Stable
class SummaryNavDependencies(
	// The sheet that asks the student to enter the password again (the sync status icon leads there).
	val onNavigateToUpdatePassword: () -> Unit = {},
	// The screens the lines of the hub lead to: the tabs, the week and the attempt, and the Statistics screen.
	val screens: SummaryScreenLinks = SummaryScreenLinks(),
	// The Schedule sheet, over Summary.
	val onOpenSchedule: () -> Unit = {},
	// The form, on the tab it belongs to.
	val onOpenForm: (form: SummaryForm) -> Unit = {}
)
