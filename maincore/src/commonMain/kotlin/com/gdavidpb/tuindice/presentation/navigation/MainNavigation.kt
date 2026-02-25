package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog

fun NavGraphBuilder.mainNavigation(
	onConfirmExitClick: () -> Unit,
	onDismissRequest: () -> Unit,
	content: @Composable (
		onConfirmExitClick: () -> Unit,
		onDismissRequest: () -> Unit
	) -> Unit
) {
	dialog<MainDestination.GooglePlayServicesUnavailableDialog>(
		typeMap = emptyMap(),
		deepLinks = emptyList(),
		dialogProperties = DialogProperties()
	) {
		content(
			onConfirmExitClick,
			onDismissRequest
		)
	}
}
