package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator

@Composable
fun CreateTermSubjectCodeChip(subjectCode: String) {
	val codeColors = remember(subjectCode) {
		CourseCodeColorGenerator.fromCode(subjectCode)
	}
	Text(
		modifier = Modifier
			.background(codeColors.containerColor, RoundedCornerShape(8.dp))
			.padding(horizontal = 10.dp, vertical = 6.dp),
		text = subjectCode,
		style = MaterialTheme.typography.bodySmall,
		fontWeight = FontWeight.Bold,
		color = codeColors.color,
		maxLines = 1
	)
}
