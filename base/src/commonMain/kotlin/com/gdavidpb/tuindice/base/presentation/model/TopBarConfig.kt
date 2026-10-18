package com.gdavidpb.tuindice.base.presentation.model

sealed class TopBarConfig(
	val actions: List<TopBarAction>
) {
	data object Summary : TopBarConfig(
		actions = listOf(
			TopBarAction.SignOutAction
		)
	)

	data object Record : TopBarConfig(
		actions = listOf(
			TopBarAction.RecordTermSelectionAction
		)
	)

	// The selected term is the current one and has a class schedule to open.
	data object RecordWithSchedule : TopBarConfig(
		actions = listOf(
			TopBarAction.RecordTermSelectionAction,
			TopBarAction.RecordScheduleAction
		)
	)

	// The current term has a class schedule to open (some subject of it has valid meetings).
	data object EvaluationsWithSchedule : TopBarConfig(
		actions = listOf(
			TopBarAction.EvaluationsScheduleAction
		)
	)

	data object Pensum : TopBarConfig(
		actions = listOf(
			TopBarAction.SearchPensumAction
		)
	)

	// The closed terms of the Statistics screen can be shared as an image.
	data object Statistics : TopBarConfig(
		actions = listOf(
			TopBarAction.StatisticsShareAction
		)
	)

	// The term of the report can be shared as an image, the way the Statistics screen shares its terms.
	data object TermReport : TopBarConfig(
		actions = listOf(
			TopBarAction.TermReportShareAction
		)
	)
}
