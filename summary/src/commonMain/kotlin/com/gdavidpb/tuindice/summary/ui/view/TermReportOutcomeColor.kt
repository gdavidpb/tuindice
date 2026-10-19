package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.summary.presentation.model.TermReportOutcomeTone

// The colour of an outcome: the approved with the approval colour, the failed with the error colour, the withdrawn with
// the available colour as the record marks a withdrawal, and the pending in the neutral one.
@Composable
fun TermReportOutcomeTone.color(): Color = when (this) {
	TermReportOutcomeTone.APPROVED -> AcademicStatusColors.approved()
	TermReportOutcomeTone.FAILED -> MaterialTheme.colorScheme.error
	TermReportOutcomeTone.WITHDRAWN -> AcademicStatusColors.available()
	TermReportOutcomeTone.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
}
