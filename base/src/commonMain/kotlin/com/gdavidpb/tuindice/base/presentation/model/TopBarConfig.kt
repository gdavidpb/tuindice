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
			TopBarAction.FetchEnrollmentProofAction
		)
	)
}
