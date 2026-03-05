package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.BaseUiTags

@Composable
fun ErrorView(
	title: String,
	message: String,
	retryText: String,
	onRetryClick: () -> Unit,
	headerContent: @Composable () -> Unit = {}
) {
	Column(
		modifier = Modifier
			.testTag(BaseUiTags.ErrorViewContainer)
			.padding(horizontal = 24.dp)
			.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		headerContent()

		Text(
			modifier = Modifier.testTag(BaseUiTags.ErrorViewTitle),
			text = title,
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Medium
		)

		Text(
			modifier = Modifier
				.testTag(BaseUiTags.ErrorViewMessage)
				.padding(vertical = 16.dp),
			text = message,
			textAlign = TextAlign.Center,
			style = MaterialTheme.typography.bodyMedium
		)

		Button(
			modifier = Modifier.testTag(BaseUiTags.ErrorViewRetryButton),
			onClick = onRetryClick
		) {
			Text(text = retryText)
		}
	}
}
