package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.ui.view.ClassPlaceLines

/**
 * The place of a week row under its title: the room on a line of its own and the schedule under it, where
 * the subject has class that day. A row whose subject has no class that day has no place, so nothing is
 * drawn under its title.
 */
@Composable
fun SummaryDetailsLine(
	place: ClassPlaceItem?,
	modifier: Modifier = Modifier
) {
	if (place != null) {
		ClassPlaceLines(
			modifier = modifier,
			place = place
		)
	}
}
