package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode

@Composable
fun recordViewModeBannerColors(mode: RecordViewMode): RecordViewModeBannerColors {
	return when (mode) {
		RecordViewMode.Historical ->
			RecordViewModeBannerColors(
				containerColor = MaterialTheme.colorScheme.primaryContainer,
				contentColor = MaterialTheme.colorScheme.onPrimaryContainer
			)

		RecordViewMode.Projection ->
			RecordViewModeBannerColors(
				containerColor = MaterialTheme.colorScheme.tertiaryContainer,
				contentColor = MaterialTheme.colorScheme.onTertiaryContainer
			)
	}
}
