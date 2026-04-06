package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.dialog.RecordViewModeInfoDialog
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.record_view_mode_banner_official
import tuindice.record.generated.resources.record_view_mode_banner_simulation
import tuindice.record.generated.resources.record_view_mode_info_button_description

@Composable
fun RecordTopBarViewModeBannerView(
	selectedMode: RecordViewMode,
	modifier: Modifier = Modifier
) {
	val colors = recordViewModeBannerColors(selectedMode)
	val isInfoDialogVisible = remember { mutableStateOf(false) }

	Box(
		modifier = modifier
			.fillMaxWidth()
			.background(color = colors.containerColor)
			.testTag(RecordUiTags.TopBarViewModeBanner)
	) {
		Row(
			modifier = Modifier
				.align(Alignment.Center)
				.padding(horizontal = 16.dp, vertical = 10.dp),
			horizontalArrangement = Arrangement.Center,
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = recordViewModeIcon(
					mode = selectedMode,
					isPrimary = true
				),
				contentDescription = null,
				tint = colors.contentColor
			)
			Text(
				text = recordViewModeBannerText(selectedMode),
				modifier = Modifier.padding(start = 8.dp),
				color = colors.contentColor,
				style = MaterialTheme.typography.labelLarge
			)
		}

		IconButton(
			modifier = Modifier
				.align(Alignment.CenterEnd)
				.testTag(RecordUiTags.TopBarViewModeInfoButton),
			onClick = {
				isInfoDialogVisible.value = true
			}
		) {
			Icon(
				imageVector = Icons.Outlined.Info,
				contentDescription = stringResource(Res.string.record_view_mode_info_button_description),
				tint = colors.contentColor
			)
		}
	}

	if (isInfoDialogVisible.value) {
		RecordViewModeInfoDialog(
			selectedMode = selectedMode,
			onDismissRequest = {
				isInfoDialogVisible.value = false
			}
		)
	}
}

@Composable
private fun recordViewModeBannerText(mode: RecordViewMode): String {
	return when (mode) {
		RecordViewMode.Official -> stringResource(Res.string.record_view_mode_banner_official)
		RecordViewMode.Simulation -> stringResource(Res.string.record_view_mode_banner_simulation)
	}
}

@Composable
fun recordViewModeBannerColors(mode: RecordViewMode): RecordViewModeBannerColors {
	return when (mode) {
		RecordViewMode.Official ->
			RecordViewModeBannerColors(
				containerColor = MaterialTheme.colorScheme.primaryContainer,
				contentColor = MaterialTheme.colorScheme.onPrimaryContainer
			)

		RecordViewMode.Simulation ->
			RecordViewModeBannerColors(
				containerColor = MaterialTheme.colorScheme.tertiaryContainer,
				contentColor = MaterialTheme.colorScheme.onTertiaryContainer
			)
	}
}

data class RecordViewModeBannerColors(
	val containerColor: Color,
	val contentColor: Color
)
