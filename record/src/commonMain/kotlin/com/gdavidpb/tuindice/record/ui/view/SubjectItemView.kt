package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.utils.Ranges
import kotlin.math.roundToInt

@Composable
fun SubjectItemView(
	modifier: Modifier = Modifier,
	item: SubjectItem,
	gradeState: MutableIntState? = null,
	onGradeChange: (newGrade: Int, isSelected: Boolean) -> Unit
) {
	val currentGrade = gradeState?.intValue ?: item.grade

	Column(
		modifier = modifier
			.testTag(RecordUiTags.subjectItem(item.subjectId))
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
				text = item.displayGradeText(currentGrade),
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
					.testTag(RecordUiTags.subjectGradeSlider(item.subjectId))
					.fillMaxWidth()
					.padding(top = 8.dp),
				value = currentGrade.toFloat(),
				steps = MAX_SUBJECT_GRADE - 1,
				valueRange = Ranges.subjectGrade,
				onValueChange = { value ->
					val newGrade = value.roundToInt()

					if (newGrade != currentGrade) {
						gradeState?.intValue = newGrade
						onGradeChange(newGrade, false)
					}
				},
				onValueChangeFinished = {
					onGradeChange(gradeState?.intValue ?: currentGrade, true)
				}
			)
		}
	}
}

private fun SubjectItem.displayGradeText(currentGrade: Int): String {
	if (currentGrade == grade) return gradeText

	return if (currentGrade == MIN_SUBJECT_GRADE) {
		"—"
	} else {
		"$currentGrade / $MAX_SUBJECT_GRADE"
	}
}
