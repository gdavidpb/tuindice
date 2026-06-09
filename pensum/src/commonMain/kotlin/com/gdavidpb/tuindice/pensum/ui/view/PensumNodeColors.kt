package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusDisplay
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeStatusType
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeVisualStyle

internal data class NodeColors(
	val container: Color,
	val border: Color,
	val chip: Color,
	val chipText: Color,
	val text: Color,
	val secondaryText: Color
)

internal data class PensumChipColors(
	val container: Color,
	val content: Color
)

internal fun String.toPensumChipColors(
	fallbackContainer: Color,
	fallbackContent: Color
): PensumChipColors {
	return CourseCodeColorGenerator.fromCodeOrNull(this)
		?.let { PensumChipColors(container = it.containerColor, content = it.color) }
		?: PensumChipColors(container = fallbackContainer, content = fallbackContent)
}

internal fun PensumNodeVisualStyle.toNodeColors(): NodeColors {
	return NodeColors(
		container = Color(containerArgb),
		border = Color(borderArgb),
		chip = Color(chipArgb),
		chipText = Color(chipTextArgb),
		text = Color(textArgb),
		secondaryText = Color(secondaryTextArgb)
	)
}

internal fun PensumNodeItem.toNodeColors(graphColors: PensumGraphColors): NodeColors {
	if (graphColors.isDark) return visualStyle.toNodeColors()

	return NodeColors(
		container = graphColors.nodeContainer,
		border = status.type.toStatusColor(graphColors),
		chip = Color(visualStyle.chipArgb),
		chipText = Color(visualStyle.chipTextArgb),
		text = graphColors.textPrimary,
		secondaryText = graphColors.textSecondary
	)
}

internal fun PensumNodeStatusDisplay.toStatusColor(graphColors: PensumGraphColors): Color {
	return if (graphColors.isDark) {
		Color(colorArgb)
	} else {
		type.toStatusColor(graphColors)
	}
}

internal fun PensumNodeStatusType.toStatusColor(graphColors: PensumGraphColors): Color {
	return when (this) {
		PensumNodeStatusType.APPROVED -> graphColors.approved
		PensumNodeStatusType.CURRENT -> graphColors.current
		PensumNodeStatusType.AVAILABLE -> graphColors.available
		PensumNodeStatusType.BLOCKED -> graphColors.blocked
	}
}
