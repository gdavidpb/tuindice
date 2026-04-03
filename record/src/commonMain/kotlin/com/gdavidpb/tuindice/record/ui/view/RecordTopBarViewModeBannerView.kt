package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.record_view_mode_banner_official
import tuindice.record.generated.resources.record_view_mode_banner_simulation

@Composable
fun RecordTopBarViewModeBannerView(
	selectedMode: RecordViewMode,
	modifier: Modifier = Modifier
) {
	val colors = recordViewModeBannerColors(selectedMode)

	Row(
		modifier = modifier
			.fillMaxWidth()
			.background(color = colors.containerColor)
			.testTag(RecordUiTags.TopBarViewModeBanner)
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
}

@Composable
private fun recordViewModeBannerText(mode: RecordViewMode): String {
	return when (mode) {
		RecordViewMode.Official -> stringResource(Res.string.record_view_mode_banner_official)
		RecordViewMode.Simulation -> stringResource(Res.string.record_view_mode_banner_simulation)
	}
}

@Composable
private fun recordViewModeBannerColors(mode: RecordViewMode): RecordViewModeBannerColors {
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

private data class RecordViewModeBannerColors(
	val containerColor: Color,
	val contentColor: Color
)
