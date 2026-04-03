package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.other
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.record_view_mode_toggle_to_official
import tuindice.record.generated.resources.record_view_mode_toggle_to_simulation
import tuindice.record.generated.resources.record_view_mode_official
import tuindice.record.generated.resources.record_view_mode_simulation

@Composable
fun RecordTopBarViewModeSwitchView(
	selectedMode: RecordViewMode,
	onModeSelected: (RecordViewMode) -> Unit,
	modifier: Modifier = Modifier
) {
	val currentModeLabel = recordViewModeLabel(selectedMode)
	val toggleDescription = recordViewModeToggleDescription(selectedMode.other())
	val colors = recordViewModeBannerColors(selectedMode)

	Box(
		modifier = modifier.testTag(RecordUiTags.TopBarViewModeSwitch)
	) {
		FilledTonalIconButton(
			modifier = Modifier
				.semantics {
					contentDescription = toggleDescription
					stateDescription = currentModeLabel
				}
				.testTag(RecordUiTags.TopBarViewModeButton)
				.padding(end = 8.dp),
			colors = IconButtonDefaults.filledTonalIconButtonColors(
				containerColor = colors.containerColor,
				contentColor = colors.contentColor
			),
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
		}
	}
}

@Composable
internal fun recordViewModeLabel(mode: RecordViewMode): String {
	return when (mode) {
		RecordViewMode.Official -> stringResource(Res.string.record_view_mode_official)
		RecordViewMode.Simulation -> stringResource(Res.string.record_view_mode_simulation)
	}
}

@Composable
internal fun recordViewModeToggleDescription(mode: RecordViewMode): String {
	return when (mode) {
		RecordViewMode.Official -> stringResource(Res.string.record_view_mode_toggle_to_official)
		RecordViewMode.Simulation -> stringResource(Res.string.record_view_mode_toggle_to_simulation)
	}
}

internal fun recordViewModeIcon(
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
