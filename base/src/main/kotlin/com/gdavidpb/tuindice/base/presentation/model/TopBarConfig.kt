package com.gdavidpb.tuindice.base.presentation.model

sealed class TopBarConfig(
	val actions: List<TopBarAction>
) {
	object Summary : TopBarConfig(
		actions = listOf(
			TopBarAction.SignOutAction
		)
	)

	object Record : TopBarConfig(
		actions = listOf(
			TopBarAction.FetchEnrollmentProofAction
		)
	)
}
