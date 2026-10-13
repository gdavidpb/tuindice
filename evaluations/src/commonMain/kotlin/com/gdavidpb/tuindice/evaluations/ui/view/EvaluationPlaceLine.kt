package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceAlpha
import com.gdavidpb.tuindice.evaluations.presentation.model.EvaluationPlaceItem

/**
 * The place an evaluation inherits from its subject's class, in one line that wraps when it must:
 * the blocks and the room first, the clock time after them and lighter, because the blocks are how
 * the university speaks of a class and the hour is only a help.
 */
@Composable
fun EvaluationPlaceLine(
	modifier: Modifier = Modifier,
	place: EvaluationPlaceItem
) {
	FlowRow(
		modifier = modifier,
		horizontalArrangement = Arrangement.spacedBy(6.dp)
	) {
		Text(
			text = place.placeText.asString(),
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)

		place.timeText?.let { time ->
			Text(
				text = time,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = TuIndiceAlpha.Muted),
				maxLines = 1
			)
		}
	}
}
