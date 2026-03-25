package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem

@Composable
fun QuarterSummaryView(
	modifier: Modifier = Modifier,
	item: QuarterItem,
	showTitle: Boolean = true,
	elevated: Boolean = true
) {
	val paddedModifier = modifier
		.fillMaxWidth()
		.padding(
			horizontal = 16.dp,
			vertical = 8.dp
		)

	if (elevated) {
		ElevatedCard(
			modifier = paddedModifier
		) {
			QuarterSummaryContent(
				modifier = Modifier.padding(8.dp),
				item = item,
				showTitle = showTitle
			)
		}
	} else {
		QuarterSummaryContent(
			modifier = paddedModifier,
			item = item,
			showTitle = showTitle
		)
	}
}

@Composable
internal fun QuarterSummaryContent(
	modifier: Modifier = Modifier,
	item: QuarterItem,
	showTitle: Boolean = true
) {
	Column(modifier = modifier) {
		if (showTitle) {
			Text(
				modifier = Modifier
					.fillMaxWidth()
					.padding(8.dp),
				text = item.nameText,
				style = MaterialTheme.typography.titleLarge,
				fontWeight = FontWeight.Black
			)
		}

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(vertical = 4.dp),
			horizontalArrangement = Arrangement.SpaceAround
		) {
			Text(
				text = item.gradeText,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Medium
			)
			Text(
				text = item.gradeSumText,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Medium
			)
			Text(
				text = item.creditsText,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.Medium
			)
		}
	}
}
