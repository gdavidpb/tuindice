package com.gdavidpb.tuindice.presentation.navigation

import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import com.gdavidpb.tuindice.ui.dialog.GooglePlayServicesDialog
import org.jetbrains.compose.resources.stringResource
import tuindice.maincore.generated.resources.Res
import tuindice.maincore.generated.resources.dialog_message_no_gms_failure
import tuindice.maincore.generated.resources.dialog_title_no_gms_failure
import tuindice.maincore.generated.resources.exit

fun NavGraphBuilder.mainNavigation(
	onConfirmExitClick: () -> Unit,
	onDismissRequest: () -> Unit
) {
	dialog<MainDestination.GooglePlayServicesUnavailableDialog>(
		typeMap = emptyMap(),
		deepLinks = emptyList(),
		dialogProperties = DialogProperties()
	) {
		GooglePlayServicesDialog(
			titleText = stringResource(Res.string.dialog_title_no_gms_failure),
			messageText = stringResource(Res.string.dialog_message_no_gms_failure),
			exitText = stringResource(Res.string.exit),
			onConfirmExitClick = onConfirmExitClick,
			onDismissRequest = onDismissRequest
		)
	}
}
