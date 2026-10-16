package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import com.gdavidpb.tuindice.base.ui.style.AcademicStatusColors
import com.gdavidpb.tuindice.base.ui.view.TuIndiceProgressBar
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * The bar of the career with the share of the pensum that is approved, in the green of "approved" everywhere else. With
 * a [description] it is a progress element for a screen reader; without one (the empty bar of the invitation) it is
 * only decoration.
 */
@Composable
fun SummaryCareerBarView(fraction: Float, description: String?) {
	val semantics = if (description != null) {
		Modifier
			.testTag(SummaryUiTags.CareerProgressBar)
			.clearAndSetSemantics {
				contentDescription = description
				progressBarRangeInfo = ProgressBarRangeInfo(current = fraction, range = 0f..1f)
			}
	} else {
		Modifier.clearAndSetSemantics { }
	}

	TuIndiceProgressBar(
		fraction = fraction,
		color = AcademicStatusColors.approved(),
		modifier = semantics
	)
}
