package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.gdavidpb.tuindice.base.ui.model.TuIndiceSubjectPointsBarTags
import com.gdavidpb.tuindice.base.ui.view.TuIndiceSubjectPointsBar
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * The points bar of a subject in the summary: the shared two-tone bar of the app, with the tags of the summary's
 * statistics on its bar and its parts, and the [description] the bar says to a screen reader.
 */
@Composable
fun SummaryEvaluatedBarView(
	subjectCode: String,
	earnedFraction: Float,
	evaluatedFraction: Float,
	description: String?,
	modifier: Modifier = Modifier
) {
	TuIndiceSubjectPointsBar(
		subjectCode = subjectCode,
		earnedFraction = earnedFraction,
		evaluatedFraction = evaluatedFraction,
		modifier = modifier
			.testTag(SummaryUiTags.termPointsBar(subjectCode))
			.clearAndSetSemantics { description?.let { contentDescription = it } },
		tags = TuIndiceSubjectPointsBarTags(
			earned = SummaryEvaluatedBarUiTags.earned(subjectCode),
			evaluated = SummaryUiTags.termPointsBarFill(subjectCode)
		)
	)
}
