package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.utils.Ranges
import kotlin.math.roundToInt

@Composable
fun SubjectItemView(
	code: String,
	name: String,
	credits: Int,
	grade: Int,
	isRetired: Boolean,
	isNoEffect: Boolean,
	isEditable: Boolean,
	onGradeChange: (newGrade: Int, isSelected: Boolean) -> Unit
) {
	val gradeString =
		if (grade != MIN_SUBJECT_GRADE)
			stringResource(id = R.string.subject_grade, grade)
		else "—"

	val statusString = when {
		isRetired -> stringResource(id = R.string.subject_retired)
		isNoEffect -> stringResource(id = R.string.subject_no_effect)
		else -> null
	}?.let { status ->
		stringResource(id = R.string.subject_status, status)
	}

	val codeString = remember(isRetired, isNoEffect) {
		buildAnnotatedString {
			withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
				append(code)
			}

			if (statusString != null) {
				append(" ")

				withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
					append(statusString)
				}
			}
		}
	}

	ConstraintLayout(
		modifier = Modifier
			.fillMaxWidth()
			.padding(
				vertical = dimensionResource(id = R.dimen.dp_8),
				horizontal = dimensionResource(id = R.dimen.dp_16)
			)
	) {
		val (
			textCode,
			textGrade,
			textName,
			textCredits,
			sliderGrade
		) = createRefs()

		Text(
			modifier = Modifier
				.constrainAs(textCode) {
					start.linkTo(parent.start)
					end.linkTo(textGrade.start)
					top.linkTo(parent.top)

					width = Dimension.fillToConstraints
				},
			text = codeString,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)

		Text(
			modifier = Modifier
				.constrainAs(textGrade) {
					end.linkTo(parent.end)
					top.linkTo(parent.top)
				},
			text = gradeString,
			fontWeight = FontWeight.Medium
		)

		Text(
			modifier = Modifier
				.constrainAs(textName) {
					start.linkTo(parent.start)
					end.linkTo(textCredits.start)
					top.linkTo(textCode.bottom)

					width = Dimension.fillToConstraints
				}
				.padding(
					end = dimensionResource(id = R.dimen.dp_16)
				),
			text = name,
			maxLines = 1,
			fontWeight = FontWeight.Light,
			overflow = TextOverflow.Ellipsis
		)

		Text(
			modifier = Modifier
				.constrainAs(textCredits) {
					end.linkTo(textGrade.end)
					top.linkTo(textGrade.bottom)
				},
			text = stringResource(id = R.string.subject_credits, credits),
			fontWeight = FontWeight.Light
		)

		if (isEditable) {
			Slider(
				modifier = Modifier
					.constrainAs(sliderGrade) {
						start.linkTo(parent.start)
						end.linkTo(parent.end)
						top.linkTo(textName.bottom)
						bottom.linkTo(parent.bottom)

						width = Dimension.fillToConstraints
					},
				value = grade.toFloat(),
				steps = MAX_SUBJECT_GRADE - 1,
				valueRange = Ranges.subjectGrade,
				onValueChange = { newGrade ->
					val roundNewGrade = newGrade.roundToInt()

					if (roundNewGrade != grade)
						onGradeChange(roundNewGrade, false)
				},
				onValueChangeFinished = {
					onGradeChange(grade, true)
				}
			)
		}
	}
}