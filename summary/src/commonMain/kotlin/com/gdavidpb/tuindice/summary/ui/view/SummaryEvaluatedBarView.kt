package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator
import com.gdavidpb.tuindice.base.ui.style.TuIndiceRadius
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

/**
 * What a subject has evaluated of its points, as a thin bar: the track in `outlineVariant` and the part
 * evaluated in the subject's own colour, the one its chip writes its code in. [fraction] is the share of the
 * total that is evaluated, held between nothing and all of it. The bar is part of its row, which says the
 * same in words, so it is not a node of its own for a screen reader.
 */
@Composable
fun SummaryEvaluatedBarView(
	subjectCode: String,
	fraction: Float,
	modifier: Modifier = Modifier
) {
	val color = remember(subjectCode) { CourseCodeColorGenerator.fromCode(subjectCode).color }

	Box(
		modifier = modifier
			.fillMaxWidth()
			.height(EvaluatedBarHeight)
			.clip(RoundedCornerShape(TuIndiceRadius.Full))
			.background(MaterialTheme.colorScheme.outlineVariant)
			.testTag(SummaryUiTags.termPointsBar(subjectCode))
	) {
		Box(
			modifier = Modifier
				.fillMaxHeight()
				.fillMaxWidth(fraction.coerceIn(0f, 1f))
				.background(color)
				.testTag(SummaryUiTags.termPointsBarFill(subjectCode))
		)
	}
}

private val EvaluatedBarHeight = 3.dp
