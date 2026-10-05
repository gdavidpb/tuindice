package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.mapper.toTodayText
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.label

/**
 * The name of a day over its column, in the table and in the week grid. Today's goes inside a pill
 * filled with the accent, in bold over it: accent-coloured text would not read on a light sheet.
 */
@Composable
fun ScheduleDayHeaderView(
	modifier: Modifier = Modifier,
	day: ScheduleDay,
	isToday: Boolean
) {
	Box(
		modifier = modifier,
		contentAlignment = Alignment.Center
	) {
		if (isToday) {
			val description = day.toTodayText().asString()

			Text(
				modifier = Modifier
					.testTag(RecordUiTags.ScheduleTodayHeader)
					.background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
					.padding(horizontal = TuIndiceSpacing.XSmall)
					.semantics { contentDescription = description },
				text = day.label(),
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onPrimary,
				textAlign = TextAlign.Center,
				maxLines = 1
			)
		} else {
			Text(
				text = day.label(),
				style = MaterialTheme.typography.labelMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				textAlign = TextAlign.Center,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}
	}
}
