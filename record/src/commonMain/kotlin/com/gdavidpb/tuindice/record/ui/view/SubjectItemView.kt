package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.persistence.utils.MIN_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.utils.Ranges
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.subject_retired
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectItemView(
	modifier: Modifier = Modifier,
	item: SubjectItem,
	gradeState: MutableIntState? = null,
	onGradeChange: (newGrade: Int, isSelected: Boolean) -> Unit
) {
	val currentGrade = gradeState?.intValue ?: item.grade
	val isRetired = (currentGrade == MIN_SUBJECT_GRADE)

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
			modifier = Modifier
				.fillMaxWidth(),
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				modifier = Modifier
					.padding(end = 12.dp)
					.weight(1f),
				text = item.nameText,
				maxLines = 1,
				fontWeight = FontWeight.SemiBold,
				style = MaterialTheme.typography.titleMedium,
				overflow = TextOverflow.Ellipsis
			)

			Box(
				modifier = Modifier
					.heightIn(min = 28.dp),
				contentAlignment = Alignment.CenterEnd
			) {
				if (isRetired) {
					Text(
						modifier = Modifier
							.padding(start = 8.dp)
							.background(
								color = MaterialTheme.colorScheme.surfaceVariant,
								shape = RoundedCornerShape(8.dp)
							)
							.padding(vertical = 4.dp, horizontal = 10.dp),
						text = stringResource(Res.string.subject_retired),
						style = MaterialTheme.typography.labelLarge
					)
				} else {
					Text(
						text = item.displayGradeText(currentGrade),
						fontWeight = FontWeight.SemiBold,
						style = MaterialTheme.typography.titleMedium
					)
				}
			}
		}

		Row(
			modifier = Modifier
				.padding(top = 8.dp)
				.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Text(
				modifier = Modifier
					.background(
						color = item.codeContainerColor,
						shape = RoundedCornerShape(8.dp)
					)
					.padding(vertical = 5.dp, horizontal = 10.dp),
				text = item.codeText,
				color = item.codeColor,
				fontWeight = FontWeight.SemiBold,
				style = MaterialTheme.typography.labelLarge
			)

			Text(
				text = item.creditsText,
				color = MaterialTheme.colorScheme.onSurfaceVariant,
				style = MaterialTheme.typography.labelLarge
			)
		}

		if (!item.isReadOnly) {
			val interactionSource = remember { MutableInteractionSource() }

			Slider(
				modifier = Modifier
					.testTag(RecordUiTags.subjectGradeSlider(item.subjectId))
					.fillMaxWidth()
					.padding(top = 8.dp),
				value = currentGrade.toFloat(),
				steps = MAX_SUBJECT_GRADE - 1,
				valueRange = Ranges.subjectGrade,
				interactionSource = interactionSource,
				thumb = {
					Box(
						modifier = Modifier
							.size(24.dp)
							.background(
								color = MaterialTheme.colorScheme.primary,
								shape = CircleShape
							)
					)
				},
				track = { sliderState ->
					SliderDefaults.Track(
						sliderState = sliderState
					)
				},
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
	if ((currentGrade == grade) && (currentGrade != MIN_SUBJECT_GRADE)) return gradeText

	return "$currentGrade / $MAX_SUBJECT_GRADE"
}
