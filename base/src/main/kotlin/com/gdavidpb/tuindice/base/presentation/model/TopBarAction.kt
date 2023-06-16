package com.gdavidpb.tuindice.base.presentation.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterAlt
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.ui.graphics.vector.ImageVector

sealed class TopBarAction(
	val action: String,
	val icon: ImageVector
) {
	object SignOutAction : TopBarAction(
		action = "sign_out",
		icon = Icons.Outlined.Logout
	)

	object FetchEnrollmentProofAction : TopBarAction(
		action = "enrollment_proof",
		icon = Icons.Outlined.FindInPage
	)

	object FilterEvaluationsAction : TopBarAction(
		action = "filter_evaluations",
		icon = Icons.Outlined.FilterAlt
	)
}