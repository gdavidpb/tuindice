package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.assistant.ui.model.CardFigure

@Composable
internal fun CardFigureColumn(figure: CardFigure, style: TextStyle) {
	Column(modifier = Modifier.clearAndSetSemantics { contentDescription = figure.contentDescription }) {
		Text(
			text = figure.value,
			style = style.copy(fontFeatureSettings = TABULAR_NUMBERS),
			fontWeight = FontWeight.Bold,
			color = MaterialTheme.colorScheme.onSurface
		)
		Text(
			text = figure.caption,
			style = MaterialTheme.typography.bodySmall,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}
