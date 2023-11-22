package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.R
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.utils.Ranges
import kotlin.math.roundToInt

@Composable
fun SubjectItemView(
	modifier: Modifier = Modifier,
	item: SubjectItem,
	onGradeChange: (newGrade: Int, isSelected: Boolean) -> Unit
) {
	ConstraintLayout(
		modifier = modifier
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
			text = item.codeAndStatusText,
			maxLines = 1,
			overflow = TextOverflow.Ellipsis
		)

		Text(
			modifier = Modifier
				.constrainAs(textGrade) {
					end.linkTo(parent.end)
					top.linkTo(parent.top)
				},
			text = item.gradeText,
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
			text = item.nameText,
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
			text = item.creditsText,
			fontWeight = FontWeight.Light
		)

		if (item.isEditable) {
			Slider(
				modifier = Modifier
					.constrainAs(sliderGrade) {
						start.linkTo(parent.start)
						end.linkTo(parent.end)
						top.linkTo(textName.bottom)
						bottom.linkTo(parent.bottom)

						width = Dimension.fillToConstraints
					},
				value = item.grade.toFloat(),
				steps = MAX_SUBJECT_GRADE - 1,
				valueRange = Ranges.subjectGrade,
				onValueChange = { newGrade ->
					val roundNewGrade = newGrade.roundToInt()

					if (roundNewGrade != item.grade)
						onGradeChange(roundNewGrade, false)
				},
				onValueChangeFinished = {
					onGradeChange(item.grade, true)
				}
			)
		}
	}
}