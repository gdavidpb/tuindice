package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.SummaryTermReportItem
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_entry_new

@Composable
fun TermReportEntryText(
	item: SummaryTermReportItem,
	modifier: Modifier = Modifier
) {
	Column(
		modifier = modifier,
		verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
		) {
			Text(
				modifier = Modifier.weight(1f, fill = false),
				text = item.title.asString(),
				style = MaterialTheme.typography.bodyLarge,
				color = MaterialTheme.colorScheme.onSurface
			)

			SummaryPillView(
				text = stringResource(Res.string.term_report_entry_new),
				isAccent = true
			)
		}

		Text(
			text = item.subtitle,
			style = MaterialTheme.typography.bodyMedium,
			color = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}
