package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsFigureItem
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * The figure of a block: its value at the headline size, what it counts beside it on the same baseline, and at the end
 * the share the block adds when it has one ("3 %"). The label wraps as a sentence when it is long.
 */
@Composable
fun SummaryStatisticsFigureView(
	figure: SummaryStatisticsFigureItem,
	modifier: Modifier = Modifier
) {
	val secondary = MaterialTheme.colorScheme.onSurfaceVariant
	val trailing = figure.trailing?.asString()

	Row(
		modifier = modifier
			.fillMaxWidth()
			.testTag(SummaryUiTags.StatisticsFigure),
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium),
		verticalAlignment = Alignment.Top
	) {
		Text(
			modifier = Modifier.alignByBaseline(),
			text = figure.value,
			style = MaterialTheme.typography.headlineMedium,
			color = MaterialTheme.colorScheme.onSurface
		)

		Text(
			modifier = Modifier.weight(1f).alignByBaseline(),
			text = figure.label.asString(),
			style = MaterialTheme.typography.bodyMedium,
			color = secondary
		)

		trailing?.let { share ->
			Text(
				modifier = Modifier.alignByBaseline(),
				text = share,
				style = MaterialTheme.typography.bodyMedium,
				color = secondary
			)
		}
	}
}
