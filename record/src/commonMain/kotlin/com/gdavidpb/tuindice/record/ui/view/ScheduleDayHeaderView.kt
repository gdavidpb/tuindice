package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.presentation.mapper.toTodayText
import com.gdavidpb.tuindice.record.presentation.model.ScheduleDay
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.label

/**
 * The name of a day over its column, in the table and in the week grid. Today's is said the way the
 * app says "the current one" everywhere else (the current term in the selector): the name in bold
 * with a small accent dot. Every header keeps the room of the dot, so the names stay level.
 */
@Composable
fun ScheduleDayHeaderView(
	modifier: Modifier = Modifier,
	day: ScheduleDay,
	isToday: Boolean
) {
	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Two),
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		if (isToday) {
			val description = day.toTodayText().asString()

			Text(
				modifier = Modifier
					.testTag(RecordUiTags.ScheduleTodayHeader)
					.semantics { contentDescription = description },
				text = day.label(),
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.Bold,
				color = MaterialTheme.colorScheme.onSurface,
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

		Box(
			modifier = Modifier
				.size(TodayDotSize)
				.clip(CircleShape)
				.background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent)
		)
	}
}

private val TodayDotSize = 5.dp
