package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.ScheduleGridDefaults
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.schedule_now_line

/**
 * The "now" line of the week grid: a dot where it starts and a line across today's column, at the
 * height of the current hour. It uses the accent's readable tone, which holds on both themes.
 */
@Composable
fun ScheduleNowLineView(modifier: Modifier = Modifier) {
	val color = MaterialTheme.colorScheme.onPrimaryContainer
	val description = stringResource(Res.string.schedule_now_line)

	Row(
		modifier = modifier
			.testTag(RecordUiTags.ScheduleNowLine)
			.height(ScheduleGridDefaults.NowDotSize)
			.semantics { contentDescription = description },
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier
				.size(ScheduleGridDefaults.NowDotSize)
				.background(color = color, shape = CircleShape)
		)

		Box(
			modifier = Modifier
				.weight(1f)
				.height(ScheduleGridDefaults.NowLineThickness)
				.background(color = color)
		)
	}
}
