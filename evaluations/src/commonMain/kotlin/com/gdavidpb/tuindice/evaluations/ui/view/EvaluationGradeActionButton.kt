package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EvaluationGradeActionButton(
	modifier: Modifier = Modifier,
	text: String,
	colors: ButtonColors,
	onClick: () -> Unit
) {
	FilledTonalButton(
		modifier = modifier,
		onClick = onClick,
		colors = colors,
		contentPadding = ButtonDefaults.TextButtonContentPadding
	) {
		Text(text = text)
	}
}
