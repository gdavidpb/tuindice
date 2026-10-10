package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.mapper.toShortNameText
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.evaluations.ui.EvaluationsUiTags
import kotlinx.datetime.DayOfWeek

@Composable
fun WeekdayHeaderRow() {
	Row(
		modifier = Modifier
			.testTag(EvaluationsUiTags.EvaluationWeekdayHeaderRow)
			.fillMaxWidth(),
		horizontalArrangement = Arrangement.spacedBy(4.dp)
	) {
		// A week from Monday to Sunday, the order the calendar grid is laid out in.
		DayOfWeek.entries.forEach { day ->
			Text(
				modifier = Modifier.weight(1f),
				text = day.toShortNameText().asString(),
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.outline,
				textAlign = TextAlign.Center
			)
		}
	}
}
