package com.gdavidpb.tuindice.pensum.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusIcon

internal fun PensumNodeStatusIcon.toImageVector(): ImageVector {
	return when (this) {
		PensumNodeStatusIcon.CHECK -> Icons.Filled.Check
		PensumNodeStatusIcon.PLAY -> Icons.Filled.PlayArrow
		PensumNodeStatusIcon.LOCK -> Icons.Outlined.Lock
		PensumNodeStatusIcon.ADD -> Icons.Outlined.Add
	}
}
