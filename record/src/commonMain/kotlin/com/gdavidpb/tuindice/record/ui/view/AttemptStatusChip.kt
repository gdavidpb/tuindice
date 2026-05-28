package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AttemptStatusChip(
	modifier: Modifier = Modifier,
	text: String
) {
	AttemptStatusBadge(
		modifier = modifier,
		text = text
	)
}
