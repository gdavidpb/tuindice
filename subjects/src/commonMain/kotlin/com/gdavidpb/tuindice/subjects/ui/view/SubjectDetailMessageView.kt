package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.ui.view.IllustratedMessageView

@Composable
fun SubjectDetailMessageView(
	modifier: Modifier = Modifier,
	title: String,
	body: String,
	actionText: String,
	onActionClick: () -> Unit,
	actionTestTag: String? = null,
	headerContent: @Composable () -> Unit = {}
) {
	IllustratedMessageView(
		modifier = modifier
			.fillMaxSize()
			.padding(
				start = 20.dp,
				top = InternalScreenDefaults.TopBarSpacing + 24.dp,
				end = 20.dp,
				bottom = 24.dp
			),
		title = title,
		message = body,
		actionLabel = actionText,
		onActionClick = onActionClick,
		actionTestTag = actionTestTag,
		titleStyle = MaterialTheme.typography.headlineSmall,
		messageContent = {
			Text(
				modifier = Modifier
					.fillMaxWidth()
					.padding(vertical = 14.dp),
				text = body,
				textAlign = TextAlign.Center,
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		},
		headerContent = { headerContent() }
	)
}
