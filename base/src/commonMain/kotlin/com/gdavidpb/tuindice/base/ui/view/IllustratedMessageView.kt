package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun IllustratedMessageView(
	modifier: Modifier = Modifier,
	title: String,
	message: String,
	actionLabel: String? = null,
	onActionClick: () -> Unit = {},
	horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
	verticalArrangement: Arrangement.Vertical = Arrangement.Top,
	titleTestTag: String? = null,
	messageTestTag: String? = null,
	actionTestTag: String? = null,
	titleStyle: TextStyle = MaterialTheme.typography.titleLarge,
	messageStyle: TextStyle = MaterialTheme.typography.bodyLarge,
	titleTextAlign: TextAlign = TextAlign.Center,
	messageTextAlign: TextAlign = TextAlign.Center,
	headerContent: @Composable ColumnScope.() -> Unit = {},
	messageContent: @Composable ColumnScope.() -> Unit = {
		Text(
			modifier = (if (messageTestTag != null) Modifier.testTag(messageTestTag) else Modifier)
				.padding(vertical = 16.dp),
			text = message,
			textAlign = messageTextAlign,
			style = messageStyle
		)
	}
) {
	Column(
		modifier = modifier,
		horizontalAlignment = horizontalAlignment,
		verticalArrangement = verticalArrangement
	) {
		headerContent()

		Text(
			modifier = if (titleTestTag != null) Modifier.testTag(titleTestTag) else Modifier,
			text = title,
			textAlign = titleTextAlign,
			style = titleStyle,
			fontWeight = FontWeight.Medium
		)

		messageContent()

		if (actionLabel != null) {
			Button(
				modifier = if (actionTestTag != null) Modifier.testTag(actionTestTag) else Modifier,
				onClick = onActionClick
			) {
				Text(text = actionLabel)
			}
		}
	}
}
