package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.base.ui.view.TuIndiceProgressBar
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * What a subject has evaluated of its points, as the shared progress bar: the part evaluated is in the subject's own
 * colour, the one its chip writes its code in. [fraction] is the share of the total that is evaluated, held between
 * nothing and all of it. The bar is part of its row, which says the same in words, so it is not a node of its own for a
 * screen reader.
 */
@Composable
fun SummaryEvaluatedBarView(
	subjectCode: String,
	fraction: Float,
	modifier: Modifier = Modifier
) {
	val color = remember(subjectCode) { CourseCodeColorGenerator.fromCode(subjectCode).color }

	TuIndiceProgressBar(
		fraction = fraction,
		color = color,
		modifier = modifier.testTag(SummaryUiTags.termPointsBar(subjectCode)),
		fillTag = SummaryUiTags.termPointsBarFill(subjectCode)
	)
}
