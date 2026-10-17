package com.gdavidpb.tuindice.assistant.ui.component.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The figure that answers the question, big, and an optional second one beside it, smaller (variant B of the owner).
@Composable
fun CardFiguresView(
	primary: CardFigure,
	modifier: Modifier = Modifier,
	secondary: CardFigure? = null,
	testTag: String? = null
) {
	Row(
		modifier = if (testTag != null) modifier.testTag(testTag) else modifier,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge),
		verticalAlignment = Alignment.Bottom
	) {
		CardFigureColumn(figure = primary, style = MaterialTheme.typography.headlineMedium)
		secondary?.let { figure -> CardFigureColumn(figure = figure, style = MaterialTheme.typography.titleLarge) }
	}
}

@Composable
private fun CardFigureColumn(figure: CardFigure, style: TextStyle) {
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

private const val TABULAR_NUMBERS = "tnum"
