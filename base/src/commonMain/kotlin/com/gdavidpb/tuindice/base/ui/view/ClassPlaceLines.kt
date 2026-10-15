package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.MeetingRoom
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

/**
 * Where a class meets, under the title of its row: the room on a line of its own, with a door icon, and under it the
 * schedule, with a clock icon. A class that names no room has no room line, so the schedule is the line that is always
 * there.
 */
@Composable
fun ClassPlaceLines(
	modifier: Modifier = Modifier,
	place: ClassPlaceItem
) {
	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		place.room?.let { room ->
			MetadataLine(icon = Icons.Outlined.MeetingRoom, text = room)
		}

		MetadataLine(icon = Icons.Outlined.Schedule, text = place.scheduleText.asString())
	}
}
