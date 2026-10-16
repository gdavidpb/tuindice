package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.FirstBaseline
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing

/**
 * What a row of the week is made of, in one shape for a class and for an evaluation: the [lead] at the
 * start, on the baseline of the first line, and beside it the [titleLine], the icon and the title of the
 * row with the chip of its subject, and its state at the end when it has one. Under the title, starting
 * where it starts, the room and the schedule of the subject where it has class that day. What does not
 * fit goes down to the next line, and nothing is cut. The block sits in the middle of the row when the row
 * is taller than it.
 */
@Composable
fun SummaryEntryView(
	lead: @Composable RowScope.() -> Unit,
	titleLine: @Composable () -> Unit,
	place: ClassPlaceItem?,
	modifier: Modifier = Modifier
) {
	Box(
		modifier = modifier,
		contentAlignment = Alignment.CenterStart
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XLarge)
		) {
			lead()

			Column(
				modifier = Modifier
					.weight(1f)
					.alignBy(FirstBaseline),
				verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
			) {
				titleLine()

				SummaryDetailsLine(place = place)
			}
		}
	}
}
