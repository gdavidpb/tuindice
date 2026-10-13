package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.ClassPlaceItem
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha

/**
 * Where a class meets, in one line that wraps when it must: the blocks and the room first, the clock
 * time after them and lighter, because the blocks are how the university speaks of a class and the
 * hour is only a help. Neither part is cut: what does not fit goes down to the next line.
 */
@Composable
fun ClassPlaceLine(
	modifier: Modifier = Modifier,
	place: ClassPlaceItem
) {
	FlowRow(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(6.dp)
	) {
		Text(
			text = place.placeText.asString(),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)

		place.timeText?.let { time ->
			Text(
				text = time.asString(),
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = TuIndiceAlpha.Muted),
				maxLines = 1
			)
		}
	}
}
