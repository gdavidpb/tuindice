package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun EvaluationWeekHeaderView(
	weekNumber: Int,
	label: String
) {
	Text(
		modifier = Modifier
			.testTag(EvaluationsUiTags.evaluationsWeekHeader(weekNumber))
			.background(MaterialTheme.colorScheme.background)
			.fillMaxWidth()
			.padding(
				top = 10.dp,
				bottom = 4.dp,
				start = 20.dp,
				end = 20.dp
			),
		text = label,
		color = MaterialTheme.colorScheme.primary,
		style = MaterialTheme.typography.titleSmall,
		fontWeight = FontWeight.Bold
	)
}
