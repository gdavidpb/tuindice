package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha

/**
 * A figure and what it is about, in one text: the figure at the title size and in the normal weight
 * ("41"), and the rest at the body size and lighter ("de 60 puntos"), on the same baseline. With no
 * [figure] only the rest is said, at the body size and lighter ("Sin notas aún"). They are one text so
 * a screen reader reads them together and a long one wraps as a sentence.
 */
@Composable
fun SummaryFigureText(
	figure: String?,
	rest: String,
	modifier: Modifier = Modifier,
	textAlign: TextAlign = TextAlign.Start
) {
	val onSurface = MaterialTheme.colorScheme.onSurface
	val secondary = onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)
	val figureStyle = MaterialTheme.typography.titleLarge.toSpanStyle()
		.copy(fontWeight = FontWeight.Normal, color = onSurface)
	val restStyle = MaterialTheme.typography.bodyMedium.toSpanStyle()
		.copy(color = secondary)

	Text(
		modifier = modifier,
		text = buildAnnotatedString {
			if (figure != null) {
				withStyle(figureStyle) { append(figure) }
				append(" ")
			}

			withStyle(restStyle) { append(rest) }
		},
		// One paragraph: with a figure its line is the figure's, and the rest sits on that line's baseline.
		style = if (figure != null) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium,
		textAlign = textAlign
	)
}
