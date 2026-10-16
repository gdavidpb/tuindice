package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsOutcome

/**
 * The colour of an outcome in the stacked bars and their legends: the approved with the approval status colour, the
 * failed with the error colour and the withdrawn with the available colour, as the record marks a withdrawal.
 */
@Composable
fun SummaryStatisticsOutcome.color(): Color = when (this) {
	SummaryStatisticsOutcome.APPROVED -> AcademicStatusColors.approved()
	SummaryStatisticsOutcome.FAILED -> MaterialTheme.colorScheme.error
	SummaryStatisticsOutcome.WITHDRAWN -> AcademicStatusColors.available()
}
