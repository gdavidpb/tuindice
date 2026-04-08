package com.gdavidpb.tuindice.record.ui.dialog

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.record_view_mode_banner_official
import tuindice.record.generated.resources.record_view_mode_banner_projection
import tuindice.record.generated.resources.record_view_mode_info_confirm
import tuindice.record.generated.resources.record_view_mode_info_message_official
import tuindice.record.generated.resources.record_view_mode_info_message_projection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordViewModeInfoDialog(
	selectedMode: RecordViewMode,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState()

	ConfirmationDialog(
		sheetState = sheetState,
		titleText = recordViewModeInfoTitle(selectedMode),
		positiveText = stringResource(Res.string.record_view_mode_info_confirm),
		onDismissRequest = onDismissRequest
	) {
		Text(
			modifier = Modifier.testTag(RecordUiTags.ViewModeInfoMessage),
			text = recordViewModeInfoMessage(selectedMode),
			style = MaterialTheme.typography.bodyLarge
		)
	}
}

@Composable
private fun recordViewModeInfoTitle(mode: RecordViewMode): String {
	return when (mode) {
		RecordViewMode.Official -> stringResource(Res.string.record_view_mode_banner_official)
		RecordViewMode.Simulation -> stringResource(Res.string.record_view_mode_banner_projection)
	}
}

@Composable
private fun recordViewModeInfoMessage(mode: RecordViewMode): String {
	return when (mode) {
		RecordViewMode.Official -> stringResource(Res.string.record_view_mode_info_message_official)
		RecordViewMode.Simulation -> stringResource(Res.string.record_view_mode_info_message_projection)
	}
}
