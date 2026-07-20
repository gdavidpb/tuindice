package com.gdavidpb.tuindice.presentation.navigation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gdavidpb.tuindice.base.presentation.navigation.TuIndiceNavActions
import com.gdavidpb.tuindice.base.presentation.navigation.dialogMetadata
import com.gdavidpb.tuindice.ui.dialog.GooglePlayServicesDialog
import org.jetbrains.compose.resources.stringResource
import tuindice.maincore.generated.resources.Res
import tuindice.maincore.generated.resources.dialog_message_no_gms_failure
import tuindice.maincore.generated.resources.dialog_title_no_gms_failure
import tuindice.maincore.generated.resources.exit

fun EntryProviderScope<NavKey>.mainEntries(
	navActions: TuIndiceNavActions,
	onConfirmExitClick: () -> Unit
) {
	entry<MainDestination.GooglePlayServicesUnavailableDialog>(metadata = dialogMetadata()) {
		GooglePlayServicesDialog(
			titleText = stringResource(Res.string.dialog_title_no_gms_failure),
			messageText = stringResource(Res.string.dialog_message_no_gms_failure),
			exitText = stringResource(Res.string.exit),
			onConfirmExitClick = onConfirmExitClick,
			onDismissRequest = { navActions.pop() }
		)
	}
}
