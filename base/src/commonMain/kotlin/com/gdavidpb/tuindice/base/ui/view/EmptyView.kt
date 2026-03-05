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
fun EmptyView(
	title: String,
	message: String,
	actionLabel: String? = null,
	onActionClick: () -> Unit = {},
	headerContent: @Composable () -> Unit = {}
) {
	Column(
		modifier = Modifier
			.testTag(BaseUiTags.EmptyViewContainer)
			.padding(horizontal = 24.dp)
			.fillMaxSize(),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		headerContent()

		Text(
			modifier = Modifier.testTag(BaseUiTags.EmptyViewTitle),
			text = title,
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Medium
		)

		Text(
			modifier = Modifier
				.testTag(BaseUiTags.EmptyViewMessage)
				.padding(vertical = 16.dp),
			text = message,
			textAlign = TextAlign.Center,
			style = MaterialTheme.typography.bodyMedium
		)

		if (actionLabel != null) {
			Button(
				modifier = Modifier.testTag(BaseUiTags.EmptyViewActionButton),
				onClick = onActionClick
			) {
				Text(text = actionLabel)
			}
		}
	}
}
