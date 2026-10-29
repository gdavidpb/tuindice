package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics

// The title of a section inside a card («Requisitos», «Distribución de notas», «Lunes»): a subtitle, written as it is,
// and a heading for a screen reader.
@Composable
fun CardSectionLabelView(
	text: String,
	modifier: Modifier = Modifier
) {
	Text(
		text = text,
		modifier = modifier.semantics { heading() },
		style = MaterialTheme.typography.titleSmall,
		color = MaterialTheme.colorScheme.onSurface
	)
}
