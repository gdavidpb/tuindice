package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.gdavidpb.tuindice.ui.dialog.GooglePlayServicesDialog
import com.gdavidpb.tuindice.ui.resource.HostUiTextProvider
import org.koin.compose.koinInject

fun NavGraphBuilder.mainNavigation(
	onConfirmExitClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	dialog<MainDestination.GooglePlayServicesUnavailableDialog>(
		typeMap = emptyMap(),
		deepLinks = emptyList(),
		dialogProperties = DialogProperties()
	) {
		val hostUiTexts = koinInject<HostUiTextProvider>().getValues()

		GooglePlayServicesDialog(
			titleText = hostUiTexts.googleServicesUnavailableTitle,
			messageText = hostUiTexts.googleServicesUnavailableMessage,
			exitText = hostUiTexts.googleServicesUnavailableExit,
			onConfirmExitClick = onConfirmExitClick,
			onDismissRequest = onDismissRequest
		)
	}
}
