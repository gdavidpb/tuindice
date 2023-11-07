package com.gdavidpb.tuindice.evaluations.ui.view.custom.grade

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.gdavidpb.tuindice.base.ui.view.WheelPicker
import com.gdavidpb.tuindice.base.ui.view.WheelPickerDefaults
import com.gdavidpb.tuindice.base.utils.extension.MeasureUnconstrainedViewSize
import com.gdavidpb.tuindice.base.utils.extension.fadingEdge
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.MAX_EVALUATION_GRADE
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.MIN_EVALUATION_GRADE
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.computeDecimals
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.computeInts
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.decimalSeparator
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.getLoopingIndex
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.toGrade
import com.gdavidpb.tuindice.evaluations.ui.view.custom.grade.utils.toGradeWheelValue

object EvaluationGradeWheelPickerDefaults {
	val ItemSeparatorWidth = 1.5.dp
	val GradeRange = MIN_EVALUATION_GRADE..MAX_EVALUATION_GRADE
}

private val fadingBrush = Brush.verticalGradient(
	0f to Color.Transparent,
	0.3f to Color.White,
	0.7f to Color.White,
	1f to Color.Transparent
)

@Composable
fun EvaluationGradeWheelPicker(
	modifier: Modifier = Modifier,
	grade: Double,
	gradeRange: ClosedFloatingPointRange<Double> = EvaluationGradeWheelPickerDefaults.GradeRange,
	onGradeChange: (grade: Double) -> Unit,
	additionalItemCount: Int = WheelPickerDefaults.AdditionalItemCount,
	textStyle: TextStyle = TextStyle.Default
) {
	val currentOnGradeChange by rememberUpdatedState(onGradeChange)

	val currentGrade = remember {
		mutableStateOf(grade.toGradeWheelValue())
	}

	val ints = remember {
		gradeRange.computeInts()
	}

	val decimals = remember {
		gradeRange.computeDecimals()
	}

	val intsLoopingStartIndex = getLoopingIndex(
		currentValue = currentGrade.value.int,
		additionalItemCount = additionalItemCount,
		values = ints
	)

	val decimalsLoopingStartIndex = getLoopingIndex(
		currentValue = currentGrade.value.decimal,
		additionalItemCount = additionalItemCount,
		values = decimals
	)

	val intsState = rememberLazyListState(
		initialFirstVisibleItemIndex = intsLoopingStartIndex
	)

	val decimalsState = rememberLazyListState(
		initialFirstVisibleItemIndex = decimalsLoopingStartIndex
	)

	MeasureUnconstrainedViewSize(viewToMeasure = {
		Text(
			text = "$MAX_EVALUATION_GRADE",
			style = textStyle
		)
	}) { measuredSize ->
		ConstraintLayout(
			modifier = modifier
		) {
			val (
				wheelInt,
				wheelSeparator,
				wheelDecimal,
				wheelBackground
			) = createRefs()

			Box(
				modifier = Modifier
					.constrainAs(wheelBackground) {
						start.linkTo(parent.start)
						end.linkTo(parent.end)
						top.linkTo(parent.top)
						bottom.linkTo(parent.bottom)

						width = Dimension.fillToConstraints
						height = Dimension.value(measuredSize.height * 1.25f)
					}
					.background(
						color = MaterialTheme.colorScheme.secondaryContainer,
						shape = CircleShape
					)
					.border(
						border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
						shape = CircleShape
					)
			)

			WheelPicker(
				modifier = Modifier
					.fadingEdge(brush = fadingBrush)
					.constrainAs(wheelInt) {
						end.linkTo(wheelSeparator.start)
						top.linkTo(parent.top)
						bottom.linkTo(parent.bottom)

						width = Dimension.value(measuredSize.width)
					},
				state = intsState,
				count = Int.MAX_VALUE,
				additionalItemCount = additionalItemCount,
				itemHeight = measuredSize.height,
				itemValidator = { index ->
					val int = ints[index % ints.size]

					val newGrade = currentGrade.value.copy(int = int).toGrade()

					newGrade in gradeRange
				},
				onItemPicked = { index ->
					val int = ints[index % ints.size]

					currentGrade.value = currentGrade.value.copy(
						int = int
					)

					currentOnGradeChange(currentGrade.value.toGrade())
				}
			) { index ->
				Text(
					modifier = Modifier
						.fillMaxWidth(),
					text = String
						.format("%02d", ints[index % ints.size]),
					style = textStyle,
					textAlign = TextAlign.End
				)
			}

			Text(
				modifier = Modifier
					.constrainAs(wheelSeparator) {
						start.linkTo(parent.start)
						end.linkTo(parent.end)
						top.linkTo(parent.top)
						bottom.linkTo(parent.bottom)
					}
					.padding(horizontal = EvaluationGradeWheelPickerDefaults.ItemSeparatorWidth),
				text = decimalSeparator,
				style = textStyle
			)

			WheelPicker(
				modifier = Modifier
					.fadingEdge(brush = fadingBrush)
					.constrainAs(wheelDecimal) {
						start.linkTo(wheelSeparator.end)
						top.linkTo(parent.top)
						bottom.linkTo(parent.bottom)

						width = Dimension.value(measuredSize.width)
					},
				state = decimalsState,
				count = Int.MAX_VALUE,
				additionalItemCount = additionalItemCount,
				itemHeight = measuredSize.height,
				itemValidator = { index ->
					val decimal = decimals[index % decimals.size]

					val newGrade = currentGrade.value.copy(decimal = decimal).toGrade()

					newGrade in gradeRange
				},
				onItemPicked = { index ->
					val decimal = decimals[index % decimals.size]

					currentGrade.value = currentGrade.value.copy(
						decimal = decimal
					)

					currentOnGradeChange(currentGrade.value.toGrade())
				}
			) { index ->
				Text(
					modifier = Modifier
						.fillMaxWidth(),
					text = String
						.format("%02d", decimals[index % decimals.size]),
					style = textStyle,
					textAlign = TextAlign.Start
				)
			}
		}
	}
}