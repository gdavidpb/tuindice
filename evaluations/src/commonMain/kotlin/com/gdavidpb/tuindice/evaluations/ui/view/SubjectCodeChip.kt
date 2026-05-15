package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SubjectCodeChip(
	modifier: Modifier = Modifier,
	subjectCode: String,
	containerColor: Color,
	contentColor: Color
) {
	Text(
		modifier = modifier
			.heightIn(min = 28.dp)
			.background(
				color = containerColor,
				shape = RoundedCornerShape(8.dp)
			)
			.padding(vertical = 5.dp, horizontal = 10.dp),
		text = subjectCode,
		color = contentColor,
		fontWeight = FontWeight.SemiBold,
		style = MaterialTheme.typography.labelLarge
	)
}
