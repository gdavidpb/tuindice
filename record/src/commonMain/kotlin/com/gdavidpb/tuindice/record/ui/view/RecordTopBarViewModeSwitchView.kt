package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.other
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.record_view_mode_official
import tuindice.record.generated.resources.record_view_mode_simulation

@Composable
fun RecordTopBarViewModeSwitchView(
	selectedMode: RecordViewMode,
	onModeSelected: (RecordViewMode) -> Unit,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier.testTag(RecordUiTags.TopBarViewModeSwitch)
	) {
		FilledTonalButton(
			modifier = Modifier
				.testTag(RecordUiTags.TopBarViewModeButton)
				.padding(end = 8.dp),
			onClick = {
				onModeSelected(selectedMode.other())
			}
		) {
			Icon(
				imageVector = recordViewModeIcon(
					mode = selectedMode,
					isPrimary = true
				),
				contentDescription = null
			)
			Text(
				text = recordViewModeLabel(selectedMode),
				modifier = Modifier.padding(start = 8.dp)
			)
		}
	}
}

@Composable
private fun recordViewModeLabel(mode: RecordViewMode): String {
	return when (mode) {
		RecordViewMode.Official -> stringResource(Res.string.record_view_mode_official)
		RecordViewMode.Simulation -> stringResource(Res.string.record_view_mode_simulation)
	}
}

private fun recordViewModeIcon(
	mode: RecordViewMode,
	isPrimary: Boolean
): ImageVector {
	return when (mode) {
		RecordViewMode.Official ->
			if (isPrimary) Icons.AutoMirrored.Filled.FactCheck else Icons.AutoMirrored.Outlined.FactCheck

		RecordViewMode.Simulation ->
			if (isPrimary) Icons.Filled.Calculate else Icons.Outlined.Calculate
	}
}
