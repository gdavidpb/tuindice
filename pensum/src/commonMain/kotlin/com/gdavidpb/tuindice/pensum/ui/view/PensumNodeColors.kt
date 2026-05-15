package com.gdavidpb.tuindice.pensum.ui.view

import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel

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

internal fun PensumScreenModel.NodeVisualStyle.toNodeColors(): NodeColors {
	return NodeColors(
		container = Color(containerArgb),
		border = Color(borderArgb),
		chip = Color(chipArgb),
		chipText = Color(chipTextArgb),
		text = Color(textArgb),
		secondaryText = Color(secondaryTextArgb)
	)
}
