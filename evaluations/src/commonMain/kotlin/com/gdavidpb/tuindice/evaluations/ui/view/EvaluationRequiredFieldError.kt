package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EvaluationRequiredFieldError(
	modifier: Modifier = Modifier,
	text: String
) {
	Text(
		modifier = modifier.padding(top = 4.dp),
		text = text,
		style = MaterialTheme.typography.bodySmall,
		color = MaterialTheme.colorScheme.error
	)
}
