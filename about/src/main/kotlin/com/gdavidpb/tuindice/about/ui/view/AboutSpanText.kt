package com.gdavidpb.tuindice.about.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.gdavidpb.tuindice.about.R

@Composable
fun AboutSpanText(
	text: String
) {
	val annotatedString = buildAnnotatedString {
		val title = text.substringBefore("\n")
		val content = text.substringAfter("\n")

		withStyle(
			style = SpanStyle(fontWeight = FontWeight.Medium)
		) { append(title) }

		append("\n$content")
	}

	Text(
		text = annotatedString,
		color = MaterialTheme.colorScheme.onSurfaceVariant,
		style = MaterialTheme.typography.bodyMedium,
		modifier = Modifier
			.padding(
				horizontal = dimensionResource(id = R.dimen.dp_16)
			)
			.fillMaxWidth()
	)
}