package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.gdavidpb.tuindice.record.R

@Composable
fun QuarterView(
	name: String,
	grade: Double,
	gradeSum: Double,
	credits: Int
) {
	Column {
		Text(
			modifier = Modifier
				.fillMaxWidth()
				.background(MaterialTheme.colorScheme.onSecondary)
				.padding(
					vertical = dimensionResource(id = R.dimen.dp_8)
				),
			text = name,
			textAlign = TextAlign.Center,
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Medium
		)

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					vertical = dimensionResource(id = R.dimen.dp_4)
				),
			horizontalArrangement = Arrangement.SpaceAround
		) {
			Text(
				text = stringResource(id = R.string.quarter_grade_diff, grade)
					.annotatedQuarterValue()
			)
			Text(
				text = stringResource(id = R.string.quarter_grade_sum, gradeSum)
					.annotatedQuarterValue()
			)
			Text(
				text = stringResource(id = R.string.quarter_credits, credits)
					.annotatedQuarterValue(-0.1f)
			)
		}
	}
}