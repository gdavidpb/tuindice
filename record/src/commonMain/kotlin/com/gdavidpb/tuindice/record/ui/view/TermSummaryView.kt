package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.presentation.model.TermItem

@Composable
fun TermSummaryView(
	modifier: Modifier = Modifier,
	item: TermItem
) {
	TermSummaryContent(
		modifier = modifier
			.fillMaxWidth()
			.padding(
				horizontal = 16.dp,
				vertical = 8.dp
			),
		item = item
	)
}
