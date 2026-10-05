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

	data object Pensum : TopBarConfig(
		actions = listOf(
			TopBarAction.SearchPensumAction
		)
	)
}
