package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.ui.RecordUiTags

@Composable
fun RecordEmptyView(
	message: String,
	highlightedParts: List<String>
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
			.testTag(RecordUiTags.EmptyContainer)
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background),
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.Center
	) {
		Column(
			modifier = Modifier.testTag(RecordUiTags.EmptyIllustration)
		) {
			Image(
				painter = rememberVectorPainter(Icons.Outlined.Info),
				contentDescription = null,
				colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
			)
		}

		Text(
			modifier = Modifier
				.testTag(RecordUiTags.EmptyMessage)
				.padding(24.dp),
			text = annotatedString,
			textAlign = TextAlign.Center,
			fontSize = MaterialTheme.typography.titleMedium.fontSize
		)
	}
}
