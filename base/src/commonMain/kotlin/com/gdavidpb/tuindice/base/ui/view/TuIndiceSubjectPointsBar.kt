package com.gdavidpb.tuindice.base.ui.view

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.ui.model.TuIndiceProgressSegment
import com.gdavidpb.tuindice.base.ui.model.TuIndiceSubjectPointsBarTags
import com.gdavidpb.tuindice.base.ui.style.CourseCodeColorGenerator

/**
 * What a subject has of its points, as the shared progress bar in two tones of the subject's own colour, the one its
 * chip writes its code in. The part it has earned ([earnedFraction]) is at the full colour, from the start. The rest
 * of what it has evaluated ([evaluatedFraction]) follows it at [EVALUATED_NOT_EARNED_ALPHA] of that colour. Both shares
 * are of the total, held between nothing and all of it, and the earned one never runs past the evaluated one.
 *
 * The bar shows no words. What a screen reader says of it is up to the [modifier] of the caller (a description set
 * with clearAndSetSemantics, as "10 de 100 evaluados"), and the row beside it reads its own points and its next
 * evaluation. Without one the bar is decoration. [tags] names its two parts for a test.
 */
@Composable
fun TuIndiceSubjectPointsBar(
	subjectCode: String,
	earnedFraction: Float,
	evaluatedFraction: Float,
	modifier: Modifier = Modifier,
	tags: TuIndiceSubjectPointsBarTags = TuIndiceSubjectPointsBarTags()
) {
	val color = remember(subjectCode) { CourseCodeColorGenerator.fromCode(subjectCode).color }
	val evaluated = evaluatedFraction.coerceIn(0f, 1f)
	val earned = earnedFraction.coerceIn(0f, evaluated)

	TuIndiceProgressBar(
		segments = listOf(
			TuIndiceProgressSegment(
				fraction = earned,
				color = color,
				tag = tags.earned
			),
			TuIndiceProgressSegment(
				fraction = evaluated - earned,
				color = color.copy(alpha = EVALUATED_NOT_EARNED_ALPHA),
				tag = tags.evaluated
			)
		),
		modifier = modifier
	)
}

/**
 * The alpha of the evaluated part that is not earned yet, drawn over the track in `outlineVariant`. At 0.4 it reads as
 * the lighter tone of the subject's colour in both themes: for every colour the generator makes, the tint stays about
 * 18 CIELAB units from the track and about 21 from the full colour. The luminance contrast is lower, since a tint is
 * a colour and not a step of light.
 */
private const val EVALUATED_NOT_EARNED_ALPHA = 0.4f
