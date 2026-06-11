package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.model.SubjectCodeChipVariant

@Composable
fun SubjectCodeChip(
	subjectCode: String,
	modifier: Modifier = Modifier,
	cornerRadius: Dp? = null,
	horizontalPadding: Dp? = null,
	verticalPadding: Dp? = null,
	variant: SubjectCodeChipVariant = SubjectCodeChipVariant.Default,
	containerColor: Color? = null,
	contentColor: Color? = null
) {
	val codeColors = remember(subjectCode) {
		CourseCodeColorGenerator.fromCode(subjectCode)
	}
	val defaults = subjectCodeChipDefaults(variant = variant)

	Text(
		modifier = modifier
			.then(defaults.minHeight?.let { Modifier.heightIn(min = it) } ?: Modifier)
			.background(
				color = containerColor ?: codeColors.containerColor,
				shape = RoundedCornerShape(cornerRadius ?: defaults.cornerRadius)
			)
			.padding(
				horizontal = horizontalPadding ?: defaults.horizontalPadding,
				vertical = verticalPadding ?: defaults.verticalPadding
			),
		text = subjectCode,
		style = defaults.textStyle,
		fontWeight = defaults.fontWeight,
		color = contentColor ?: codeColors.color,
		maxLines = 1
	)
}

@Composable
private fun subjectCodeChipDefaults(variant: SubjectCodeChipVariant): SubjectCodeChipDefaults {
	return when (variant) {
		SubjectCodeChipVariant.Default -> SubjectCodeChipDefaults(
			cornerRadius = TuIndiceRadius.Small,
			horizontalPadding = TuIndiceSpacing.Large,
			verticalPadding = TuIndiceSpacing.Small,
			textStyle = MaterialTheme.typography.bodySmall,
			fontWeight = FontWeight.Bold
		)

		SubjectCodeChipVariant.Dense -> SubjectCodeChipDefaults(
			minHeight = 28.dp,
			cornerRadius = TuIndiceRadius.Small,
			horizontalPadding = TuIndiceSpacing.Large,
			verticalPadding = 5.dp,
			textStyle = MaterialTheme.typography.labelLarge,
			fontWeight = FontWeight.SemiBold
		)

		SubjectCodeChipVariant.GraphNode -> SubjectCodeChipDefaults(
			cornerRadius = TuIndiceRadius.Small,
			horizontalPadding = TuIndiceSpacing.Large,
			verticalPadding = TuIndiceSpacing.Small,
			textStyle = MaterialTheme.typography.labelLarge,
			fontWeight = FontWeight.SemiBold
		)
	}
}

private data class SubjectCodeChipDefaults(
	val minHeight: Dp? = null,
	val cornerRadius: Dp,
	val horizontalPadding: Dp,
	val verticalPadding: Dp,
	val textStyle: TextStyle,
	val fontWeight: FontWeight
)
