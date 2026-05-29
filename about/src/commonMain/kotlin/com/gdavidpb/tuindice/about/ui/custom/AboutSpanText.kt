package com.gdavidpb.tuindice.about.ui.custom

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.about.ui.AboutUiTags
import com.gdavidpb.tuindice.base.ui.view.appendWithBoldMarkers

@Composable
fun AboutSpanText(
	text: String,
	modifier: Modifier = Modifier
) {
	val annotatedString = buildAnnotatedString {
		val title = text.substringBefore("\n")
		val content = text.substringAfter(
			delimiter = "\n",
			missingDelimiterValue = ""
		)

		withStyle(
			style = SpanStyle(fontWeight = FontWeight.Medium)
		) {
			appendWithBoldMarkers(title)
		}

		if (content.isNotEmpty()) {
			append("\n")
			appendWithBoldMarkers(content)
		}
	}

	Text(
		text = annotatedString,
		color = MaterialTheme.colorScheme.onSurfaceVariant,
		style = MaterialTheme.typography.bodyMedium,
		modifier = modifier
			.testTag(AboutUiTags.SpanText)
			.padding(horizontal = 16.dp)
			.fillMaxWidth()
	)
}
