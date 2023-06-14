package com.gdavidpb.tuindice.base.presentation.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.ui.graphics.vector.ImageVector

sealed class TopBarConfig(
	val icon: ImageVector
) {
	object Summary : TopBarConfig(
		icon = Icons.Outlined.Logout
	)

	object Record : TopBarConfig(
		icon = Icons.Outlined.FindInPage
	)
}
