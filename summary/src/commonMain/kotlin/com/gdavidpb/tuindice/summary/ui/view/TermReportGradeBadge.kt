package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.summary.presentation.model.TermReportOutcomeTone
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.term_report_subjects_no_grade

// The grade in a disc of the colour of its outcome. A subject with no grade yet shows a dash in the neutral colour.
@Composable
fun TermReportGradeBadge(
	grade: String?,
	tone: TermReportOutcomeTone,
	modifier: Modifier = Modifier
) {
	val color = tone.color()

	Box(
		modifier = modifier
			.size(GradeBadgeSize)
			.background(color = color.copy(alpha = BADGE_ALPHA), shape = CircleShape),
		contentAlignment = Alignment.Center
	) {
		Text(
			text = grade ?: stringResource(Res.string.term_report_subjects_no_grade),
			style = MaterialTheme.typography.titleSmall,
			fontWeight = FontWeight.Bold,
			color = color
		)
	}
}

private val GradeBadgeSize = 32.dp
private const val BADGE_ALPHA = 0.16f
