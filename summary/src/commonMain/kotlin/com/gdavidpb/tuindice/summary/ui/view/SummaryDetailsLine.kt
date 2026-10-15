package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.ui.model.SubjectCodeChipVariant
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.ClassPlaceLine
import com.gdavidpb.tuindice.base.ui.view.SubjectCodeChip

/**
 * The chip of the subject of a week row, and beside it, where the subject has class that day, its place: the blocks and
 * the room, and the hour for a class. The place goes down to the next line as a whole when it does not fit beside the
 * chip; inside it, the blocks and the room wrap as they always do.
 */
@Composable
fun SummaryDetailsLine(
	subjectCode: String,
	place: ClassPlaceItem?,
	modifier: Modifier = Modifier
) {
	FlowRow(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall),
		itemVerticalAlignment = Alignment.CenterVertically
	) {
		SubjectCodeChip(
			subjectCode = subjectCode,
			variant = SubjectCodeChipVariant.Dense
		)

		if (place != null) {
			ClassPlaceLine(place = place)
		}
	}
}
