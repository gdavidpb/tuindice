package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.outlined.FactCheck
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode

fun recordViewModeIcon(
	mode: RecordViewMode,
	isPrimary: Boolean
): ImageVector {
	return when (mode) {
		RecordViewMode.Historical ->
			if (isPrimary) Icons.AutoMirrored.Filled.FactCheck else Icons.AutoMirrored.Outlined.FactCheck

		RecordViewMode.Projection ->
			if (isPrimary) Icons.Filled.Calculate else Icons.Outlined.Calculate
	}
}
