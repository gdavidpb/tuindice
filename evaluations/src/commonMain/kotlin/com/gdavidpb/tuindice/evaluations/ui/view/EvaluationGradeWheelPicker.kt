package com.gdavidpb.tuindice.evaluations.ui.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.view.WheelPicker
import com.gdavidpb.tuindice.base.ui.view.WheelPickerDefaults
import com.gdavidpb.tuindice.base.utils.extension.MeasureUnconstrainedViewSize
import com.gdavidpb.tuindice.base.utils.extension.fadingEdge
import com.gdavidpb.tuindice.evaluations.ui.model.*

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

	val currentGrade = remember(grade) {
		mutableStateOf(grade.toGradeWheelValue())
	}

	val ints = remember(gradeRange.start, gradeRange.endInclusive) {
		gradeRange.computeInts()
	}

	val decimals = remember(gradeRange.start, gradeRange.endInclusive) {
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
	}) { itemSize ->
		MeasureUnconstrainedViewSize(viewToMeasure = {
			Text(
				text = decimalSeparator,
				style = textStyle
			)
		}) { separatorSize ->
			val selectedFrameWidth =
				(itemSize.width * 2) +
					separatorSize.width +
					(EvaluationGradeWheelPickerDefaults.ItemSeparatorWidth * 2)

			Box(modifier = modifier) {
				Box(
					modifier = Modifier
						.align(Alignment.Center)
						.width(selectedFrameWidth)
						.height(itemSize.height * 1.25f)
						.background(
							color = MaterialTheme.colorScheme.secondaryContainer,
							shape = CircleShape
						)
						.border(
							border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
							shape = CircleShape
						)
				)

				Row(
					modifier = Modifier.align(Alignment.Center),
					verticalAlignment = Alignment.CenterVertically
				) {
					WheelPicker(
						modifier = Modifier
							.width(itemSize.width)
							.fadingEdge(brush = fadingBrush),
						state = intsState,
						count = Int.MAX_VALUE,
						additionalItemCount = additionalItemCount,
						itemHeight = itemSize.height,
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
							modifier = Modifier.fillMaxWidth(),
							text = ints[index % ints.size].toString().padStart(2, '0'),
							style = textStyle,
							textAlign = TextAlign.End
						)
					}

					Text(
						modifier = Modifier
							.padding(horizontal = EvaluationGradeWheelPickerDefaults.ItemSeparatorWidth),
						text = decimalSeparator,
						style = textStyle
					)

					WheelPicker(
						modifier = Modifier
							.width(itemSize.width)
							.fadingEdge(brush = fadingBrush),
						state = decimalsState,
						count = Int.MAX_VALUE,
						additionalItemCount = additionalItemCount,
						itemHeight = itemSize.height,
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
							modifier = Modifier.fillMaxWidth(),
							text = decimals[index % decimals.size].toString().padStart(2, '0'),
							style = textStyle,
							textAlign = TextAlign.Start
						)
					}
				}
			}
		}
	}
}
