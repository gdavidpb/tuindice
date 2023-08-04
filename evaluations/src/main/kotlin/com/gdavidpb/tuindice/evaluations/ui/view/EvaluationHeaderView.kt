package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.evaluations.R

@Composable
fun EvaluationHeaderView(
	label: String
) {
	Box(
		modifier = Modifier
			.background(MaterialTheme.colorScheme.background)
	) {
		Text(
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					vertical = dimensionResource(id = R.dimen.dp_8),
					horizontal = dimensionResource(id = R.dimen.dp_12)
				),
			text = label,
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Black,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)
	}
}