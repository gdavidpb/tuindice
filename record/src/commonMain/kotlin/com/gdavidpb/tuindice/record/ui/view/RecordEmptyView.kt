package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun RecordEmptyView(
	message: String,
	highlightedParts: List<String>,
	illustrationContent: @Composable () -> Unit = {}
) {
	val annotatedString = remember(message, highlightedParts) {
		buildAnnotatedString {
			append(message)

			highlightedParts
				.filter { it.isNotBlank() }
				.forEach { part ->
					val start = message.indexOf(part)

					if (start >= 0) {
						addStyle(
							style = SpanStyle(fontWeight = FontWeight.Medium),
							start = start,
							end = start + part.length
						)
					}
				}
		}
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		illustrationContent()

		Text(
			modifier = Modifier.padding(24.dp),
			text = annotatedString,
			textAlign = TextAlign.Center,
			fontSize = MaterialTheme.typography.titleMedium.fontSize
		)
	}
}
