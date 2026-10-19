package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.summary.presentation.model.TermReportItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_assistant_empty

// The sections in order. A report with no sections says its title, or that it has no text yet.
@Composable
fun TermReportSections(report: TermReportItem) {
	if (report.sections.isEmpty()) {
		Text(
			text = report.title.ifEmpty { stringResource(Res.string.term_report_assistant_empty) },
			style = MaterialTheme.typography.titleSmall,
			color = MaterialTheme.colorScheme.onSurface
		)
	}

	report.sections.forEachIndexed { index, section ->
		Column(
			modifier = Modifier.testTag(TermReportUiTags.termReportSection(index)),
			verticalArrangement = Arrangement.spacedBy(TuIndiceSpacing.XSmall)
		) {
			Text(
				text = section.title,
				style = MaterialTheme.typography.titleSmall,
				color = MaterialTheme.colorScheme.onSurface
			)

			Text(
				text = section.text,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
	}
}
