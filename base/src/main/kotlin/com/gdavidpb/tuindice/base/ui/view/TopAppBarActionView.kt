package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FindInPage
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.presentation.model.TopBarActionConfig

@Composable
fun TopAppBarActionView(
	topBarActionConfig: TopBarActionConfig,
	onSignOutClick: () -> Unit,
	onFetchEnrollmentProofClick: () -> Unit
) {
	when (topBarActionConfig) {
		TopBarActionConfig.Summary ->
			IconButton(onClick = onSignOutClick) {
				Icon(
					imageVector = Icons.Outlined.Logout,
					contentDescription = null
				)
			}

		TopBarActionConfig.Record ->
			IconButton(onClick = onFetchEnrollmentProofClick) {
				Icon(
					imageVector = Icons.Outlined.FindInPage,
					contentDescription = null
				)
			}
	}
}