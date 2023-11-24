package com.gdavidpb.tuindice.base.presentation.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.ui.graphics.vector.ImageVector

sealed class TopBarAction(
	val action: String,
	val icon: ImageVector
) {
	data object SignOutAction : TopBarAction(
		action = "sign_out",
		icon = Icons.AutoMirrored.Outlined.Logout
	)

	data object FetchEnrollmentProofAction : TopBarAction(
		action = "enrollment_proof",
		icon = Icons.Outlined.FindInPage
	)
}