package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.gdavidpb.tuindice.base.ui.BaseUiTags
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

// The text of a row: its first line is the title, in the medium weight, and the lines after a break are the detail.
@Composable
fun TuIndiceSpanText(
	text: String,
	modifier: Modifier = Modifier,
	color: Color = MaterialTheme.colorScheme.onSurfaceVariant
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
		color = color,
		style = MaterialTheme.typography.bodyMedium,
		modifier = modifier
			.testTag(BaseUiTags.SpanText)
			.padding(horizontal = TuIndiceSpacing.Section)
			.fillMaxWidth()
	)
}
