package com.gdavidpb.tuindice.subjects.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults

@Composable
fun SubjectDetailMessageView(
	modifier: Modifier = Modifier,
	title: String,
	body: String,
	actionText: String,
	onActionClick: () -> Unit,
	actionTestTag: String? = null
) {
	Column(
		modifier = modifier
			.fillMaxSize()
			.padding(
				start = 20.dp,
				top = InternalScreenDefaults.TopBarSpacing + 24.dp,
				end = 20.dp,
				bottom = 24.dp
			),
		verticalArrangement = Arrangement.spacedBy(14.dp)
	) {
		Text(
			text = title,
			style = MaterialTheme.typography.headlineSmall,
			fontWeight = FontWeight.SemiBold
		)

		Text(
			text = body,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.End
		) {
			TextButton(
				onClick = onActionClick,
				modifier = if (actionTestTag != null) Modifier.testTag(actionTestTag) else Modifier
			) {
				Text(actionText)
			}
		}
	}
}
