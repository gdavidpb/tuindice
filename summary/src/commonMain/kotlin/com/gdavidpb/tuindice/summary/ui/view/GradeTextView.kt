package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.gdavidpb.tuindice.base.ui.style.LocalTuIndiceAnimationsEnabled
import com.gdavidpb.tuindice.base.utils.extension.DecelerateEasing
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags

private const val DECIMALS_GRADE = 4
private const val DECIMALS_MAX_GRADE = 1
private const val TIME_ANIMATION = 750

const val MAX_GRADE = 5.0f

/**
 * A grade over its scale, as "4.2500 / 5.0": the grade at four decimals, and the scale after it at half the size. It is
 * written in [style], which is the headline of the header by default, and it counts up to [grade] in the standard time
 * unless the animations are off, when it is written at once. [tag] names the text, so each surface that shows a grade
 * names its own, and the header keeps the tag its tests read.
 */
@Composable
fun GradeTextView(
	modifier: Modifier = Modifier,
	grade: Float,
	maxGrade: Float = MAX_GRADE,
	style: TextStyle = MaterialTheme.typography.headlineLarge,
	tag: String = SummaryUiTags.GradeText
) {
	val animationsEnabled = LocalTuIndiceAnimationsEnabled.current
	val gradeAnimation = remember {
		Animatable(0f)
	}
	val shownGrade = if (animationsEnabled) gradeAnimation.value else grade

	LaunchedEffect(grade, animationsEnabled) {
		if (animationsEnabled) {
			gradeAnimation.animateTo(
				targetValue = grade,
				animationSpec = tween(
					durationMillis = TIME_ANIMATION,
					easing = DecelerateEasing
				)
			)
		}
	}

	val annotatedString = buildAnnotatedString {
		val gradeText = shownGrade.formatGrade(decimals = DECIMALS_GRADE)
		val maxGradeText = maxGrade.formatGrade(decimals = DECIMALS_MAX_GRADE)

		withStyle(
			style = SpanStyle(
				fontWeight = FontWeight.Light
			)
		) {
			append(gradeText)
		}

		withStyle(
			style = SpanStyle(
				fontSize = style.fontSize * 0.5f,
				fontWeight = FontWeight.Light
			)
		) {
			append(" / $maxGradeText")
		}
	}

	Text(
		modifier = modifier.testTag(tag),
		text = annotatedString,
		style = style
	)
}
