package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig

@Composable
fun TopAppBarActionView(
	topBarConfig: TopBarConfig,
	onSignOutClick: () -> Unit,
	onFetchEnrollmentProofClick: () -> Unit
) {
	when (topBarConfig) {
		TopBarConfig.Summary ->
			IconButton(onClick = onSignOutClick) {
				Icon(
					imageVector = topBarConfig.icon,
					contentDescription = null
				)
			}

		TopBarConfig.Record ->
			IconButton(onClick = onFetchEnrollmentProofClick) {
				Icon(
					imageVector = topBarConfig.icon,
					contentDescription = null
				)
			}
	}
}