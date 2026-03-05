package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.mapper.localizedShortWeekdayNames
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags

@Composable
fun WeekdayHeaderRow() {
	val weekdayLabels = remember { localizedShortWeekdayNames() }

	Row(
		modifier = Modifier
			.testTag(EvaluationsUiTags.EvaluationWeekdayHeaderRow)
			.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(4.dp)
	) {
		weekdayLabels.forEach { label ->
			Text(
				modifier = Modifier.weight(1f),
				text = label,
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.outline,
				textAlign = TextAlign.Center
			)
		}
	}
}
