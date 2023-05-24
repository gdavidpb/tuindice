package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.gdavidpb.tuindice.base.utils.extension.DecelerateEasing
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.summary.R

private const val DECIMALS_GRADE = 4
private const val DECIMALS_MAX_GRADE = 1
private const val TIME_ANIMATION = 750

const val MAX_GRADE = 5.0f

@Composable
fun GradeTextView(
	modifier: Modifier = Modifier,
	grade: Float,
	maxGrade: Float = MAX_GRADE
) {
	val gradeAnimation = remember {
		Animatable(0f)
	}

	LaunchedEffect(Unit) {
		gradeAnimation.animateTo(
			targetValue = grade,
			animationSpec = tween(
				durationMillis = TIME_ANIMATION,
				easing = DecelerateEasing
			)
		)
	}

	val annotatedString = buildAnnotatedString {
		val gradeText = gradeAnimation.value.formatGrade(decimals = DECIMALS_GRADE)
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
				fontSize = MaterialTheme.typography.headlineLarge.fontSize * 0.5f,
				fontWeight = FontWeight.Light
			)
		) {
			append("/$maxGradeText")
		}
	}

	Text(
		modifier = modifier
			.padding(vertical = dimensionResource(id = R.dimen.dp_8)),
		text = annotatedString,
		style = MaterialTheme.typography.headlineLarge
	)
}