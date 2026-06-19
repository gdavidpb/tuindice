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

	data object RecordWithEnrollmentProof : TopBarConfig(
		actions = listOf(
			TopBarAction.RecordTermSelectionAction,
			TopBarAction.FetchEnrollmentProofAction
		)
	)

	data object Pensum : TopBarConfig(
		actions = listOf(
			TopBarAction.SearchPensumAction
		)
	)
}
