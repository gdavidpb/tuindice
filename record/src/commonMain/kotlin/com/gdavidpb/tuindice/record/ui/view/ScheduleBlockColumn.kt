package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.record.ui.model.ScheduleGridDefaults

/** The block numbers of the weekly grid, each centered on its row and kept clear of the first day's cells. */
@Composable
fun ScheduleBlockColumn(
	modifier: Modifier = Modifier,
	blockCount: Int
) {
	Column(
		modifier = modifier.width(ScheduleGridDefaults.BlockColumnWidth),
		verticalArrangement = Arrangement.spacedBy(ScheduleGridDefaults.BlockRowGap)
	) {
		repeat(blockCount) { index ->
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.height(ScheduleGridDefaults.BlockRowHeight)
					.padding(end = TuIndiceSpacing.XSmall),
				contentAlignment = Alignment.CenterEnd
			) {
				Text(
					text = (index + 1).toString(),
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant,
					textAlign = TextAlign.End,
					maxLines = 1
				)
			}
		}
	}
}
