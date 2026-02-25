package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.utils.Ranges
import kotlin.math.roundToInt

@Composable
fun SubjectItemView(
	modifier: Modifier = Modifier,
	item: SubjectItem,
	onGradeChange: (newGrade: Int, isSelected: Boolean) -> Unit
) {
	val grade = remember {
		mutableIntStateOf(item.grade)
	}

	Column(
		modifier = modifier
			.fillMaxWidth()
			.padding(
				vertical = 8.dp,
				horizontal = 16.dp
			)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				modifier = Modifier
					.weight(1f)
					.padding(end = 12.dp),
				text = item.codeAndStatusText,
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)

			Text(
				text = item.gradeText,
				fontWeight = FontWeight.Medium
			)
		}

		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(top = 4.dp),
			horizontalArrangement = Arrangement.SpaceBetween
		) {
			Text(
				modifier = Modifier
					.weight(1f)
					.padding(end = 12.dp),
				text = item.nameText,
				maxLines = 1,
				fontWeight = FontWeight.Light,
				overflow = TextOverflow.Ellipsis
			)

			Text(
				text = item.creditsText,
				fontWeight = FontWeight.Light
			)
		}

		if (!item.isReadOnly) {
			Slider(
				modifier = Modifier
					.testTag("subject_grade_slider_${item.subjectId}")
					.fillMaxWidth()
					.padding(top = 8.dp),
				value = item.grade.toFloat(),
				steps = MAX_SUBJECT_GRADE - 1,
				valueRange = Ranges.subjectGrade,
				onValueChange = { value ->
					val newGrade = value.roundToInt()
					val oldGrade = grade.intValue

					if (newGrade != oldGrade) {
						grade.intValue = newGrade
						onGradeChange(newGrade, false)
					}
				},
				onValueChangeFinished = {
					onGradeChange(grade.intValue, true)
				}
			)
		}
	}
}
