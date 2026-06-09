package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator

@Composable
fun SubjectCodeChip(
	subjectCode: String,
	modifier: Modifier = Modifier,
	cornerRadius: Dp = 8.dp,
	horizontalPadding: Dp = 10.dp,
	verticalPadding: Dp = 6.dp
) {
	val codeColors = remember(subjectCode) {
		CourseCodeColorGenerator.fromCode(subjectCode)
	}
	Text(
		modifier = modifier
			.background(codeColors.containerColor, RoundedCornerShape(cornerRadius))
			.padding(horizontal = horizontalPadding, vertical = verticalPadding),
		text = subjectCode,
		style = MaterialTheme.typography.bodySmall,
		fontWeight = FontWeight.Bold,
		color = codeColors.color,
		maxLines = 1
	)
}
