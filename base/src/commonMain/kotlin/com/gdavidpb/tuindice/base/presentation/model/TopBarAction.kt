package com.gdavidpb.tuindice.base.presentation.model

sealed class TopBarAction(
	val action: String
) {
	data object SignOutAction : TopBarAction(
		action = "sign_out"
	)

	data object FetchEnrollmentProofAction : TopBarAction(
		action = "enrollment_proof"
	)

	data object SearchPensumAction : TopBarAction(
		action = "search_pensum"
	)

	data object ChangePensumAction : TopBarAction(
		action = "change_pensum"
	)
}
