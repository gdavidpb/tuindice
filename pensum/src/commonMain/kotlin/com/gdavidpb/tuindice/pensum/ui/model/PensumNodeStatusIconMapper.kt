package com.gdavidpb.tuindice.pensum.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusIcon

internal val PensumCurrentRouteIcon: ImageVector
	get() = Icons.Outlined.BookmarkBorder

internal fun PensumNodeStatusIcon.toImageVector(): ImageVector {
	return when (this) {
		PensumNodeStatusIcon.CHECK -> Icons.Filled.Check
		PensumNodeStatusIcon.CURRENT_ROUTE -> PensumCurrentRouteIcon
		PensumNodeStatusIcon.LOCK -> Icons.Outlined.Lock
		PensumNodeStatusIcon.ADD -> Icons.Outlined.Add
	}
}
