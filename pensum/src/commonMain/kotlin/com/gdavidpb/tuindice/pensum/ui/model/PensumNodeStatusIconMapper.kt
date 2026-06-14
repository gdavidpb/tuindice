package com.gdavidpb.tuindice.pensum.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.RadioButtonChecked
import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusDisplay
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusIcon
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType

internal val PensumCurrentRouteIcon: ImageVector
	get() = Icons.Outlined.RadioButtonChecked

internal data class PensumStatusIconVisual(
	val imageVector: ImageVector,
	val hasBuiltInContainer: Boolean
)

internal fun PensumNodeStatusDisplay.toStatusIconVisual(): PensumStatusIconVisual {
	return PensumStatusIconVisual(
		imageVector = icon.toImageVector(),
		hasBuiltInContainer = type.hasBuiltInIconContainer()
	)
}

internal fun PensumNodeStatusType.toStatusIconVisual(): PensumStatusIconVisual {
	return PensumStatusIconVisual(
		imageVector = toStatusIcon().toImageVector(),
		hasBuiltInContainer = hasBuiltInIconContainer()
	)
}

internal fun PensumNodeStatusIcon.toImageVector(): ImageVector {
	return when (this) {
		PensumNodeStatusIcon.CHECK -> Icons.Filled.Check
		PensumNodeStatusIcon.CURRENT_ROUTE -> PensumCurrentRouteIcon
		PensumNodeStatusIcon.LOCK -> Icons.Outlined.Lock
		PensumNodeStatusIcon.ADD -> Icons.Outlined.Add
	}
}

private fun PensumNodeStatusType.toStatusIcon(): PensumNodeStatusIcon {
	return when (this) {
		PensumNodeStatusType.APPROVED -> PensumNodeStatusIcon.CHECK
		PensumNodeStatusType.CURRENT -> PensumNodeStatusIcon.CURRENT_ROUTE
		PensumNodeStatusType.AVAILABLE -> PensumNodeStatusIcon.ADD
		PensumNodeStatusType.BLOCKED -> PensumNodeStatusIcon.LOCK
	}
}

private fun PensumNodeStatusType.hasBuiltInIconContainer(): Boolean {
	return this == PensumNodeStatusType.CURRENT
}
