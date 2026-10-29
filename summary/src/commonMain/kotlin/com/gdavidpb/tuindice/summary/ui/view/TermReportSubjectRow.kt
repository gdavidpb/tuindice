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
import com.gdavidpb.tuindice.base.ui.model.SubjectCodeChipVariant
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.SubjectCodeChip
import com.gdavidpb.tuindice.summary.presentation.model.TermReportSubjectItem
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags

@Composable
fun TermReportSubjectRow(
	row: TermReportSubjectItem,
	modifier: Modifier = Modifier
) {
	Row(
		modifier = modifier
			.fillMaxWidth()
			.testTag(TermReportUiTags.termReportSubject(row.subjectCode)),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(TuIndiceSpacing.Medium)
	) {
		SubjectCodeChip(
			subjectCode = row.subjectCode,
			variant = SubjectCodeChipVariant.Dense
		)

		Text(
			modifier = Modifier.weight(1f),
			text = row.subjectName,
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurface
		)

		TermReportGradeBadge(grade = row.grade, tone = row.tone)
	}
}
