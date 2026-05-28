package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.record_view_mode_historical
import tuindice.record.generated.resources.record_view_mode_projection
import tuindice.record.generated.resources.record_view_mode_toggle_to_historical
import tuindice.record.generated.resources.record_view_mode_toggle_to_projection

@Composable
fun RecordTopBarViewModeSwitchView(
	selectedMode: RecordViewMode,
	onModeSelected: (RecordViewMode) -> Unit,
	modifier: Modifier = Modifier
) {
	val currentModeLabel = recordViewModeLabel(selectedMode)
	val toggleDescription = recordViewModeToggleDescription(selectedMode.otherMode())
	val colors = recordViewModeBannerColors(selectedMode)

	Box(
		modifier = modifier
			.testTag(RecordUiTags.TopBarViewModeSwitch)
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
				onModeSelected(selectedMode.otherMode())
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

private fun RecordViewMode.otherMode(): RecordViewMode {
	return when (this) {
		RecordViewMode.Historical -> RecordViewMode.Projection
		RecordViewMode.Projection -> RecordViewMode.Historical
	}
}

@Composable
private fun recordViewModeLabel(mode: RecordViewMode): String {
	return when (mode) {
		RecordViewMode.Historical -> stringResource(Res.string.record_view_mode_historical)
		RecordViewMode.Projection -> stringResource(Res.string.record_view_mode_projection)
	}
}

@Composable
private fun recordViewModeToggleDescription(mode: RecordViewMode): String {
	return when (mode) {
		RecordViewMode.Historical -> stringResource(Res.string.record_view_mode_toggle_to_historical)
		RecordViewMode.Projection -> stringResource(Res.string.record_view_mode_toggle_to_projection)
	}
}
