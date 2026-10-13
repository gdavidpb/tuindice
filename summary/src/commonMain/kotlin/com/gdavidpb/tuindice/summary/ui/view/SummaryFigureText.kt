package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha

/**
 * A figure and what it is about, in one text: the figure heavy ("41") and the rest lighter ("de 60
 * puntos"). With no [figure] only the rest is said, lighter ("Sin notas aún"). They are one text so
 * a screen reader reads them together and a long one wraps as a sentence.
 */
@Composable
fun SummaryFigureText(
	figure: String?,
	rest: String,
	modifier: Modifier = Modifier,
	textAlign: TextAlign = TextAlign.Start
) {
	val secondary = MaterialTheme.colorScheme.onSurface.copy(alpha = TuIndiceAlpha.Deemphasis)

	Text(
		modifier = modifier,
		text = buildAnnotatedString {
			if (figure != null) {
				withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(figure) }
				append(" ")
			}

			withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = secondary)) { append(rest) }
		},
		style = if (figure != null) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
		textAlign = textAlign
	)
}
