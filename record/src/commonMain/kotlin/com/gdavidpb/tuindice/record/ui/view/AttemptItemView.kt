package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
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
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.academiccore.domain.model.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.model.AttemptItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.AttemptItemBadge
import com.gdavidpb.tuindice.record.ui.model.toAttemptItemDisplay
import com.gdavidpb.tuindice.record.utils.Ranges
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.attempt_approved
import tuindice.record.generated.resources.attempt_failed
import tuindice.record.generated.resources.attempt_retired
import tuindice.record.generated.resources.attempt_unreported
import tuindice.record.generated.resources.attempt_without_effect
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttemptItemView(
	modifier: Modifier = Modifier,
	item: AttemptItem,
	gradeState: MutableIntState? = null,
	onGradeChange: (newGrade: Int?, newOutcome: AttemptOutcome?, isSelected: Boolean) -> Unit
) {
	val currentGrade = gradeState?.intValue ?: item.grade
	val display = remember(item, currentGrade) {
		item.toAttemptItemDisplay(currentGrade = currentGrade)
	}
	val isQualitative = item.gradingMode == GradingMode.QUALITATIVE_PASS_FAIL

	Column(
		modifier = modifier
			.testTag(RecordUiTags.attemptItem(item.attemptId))
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
				when (display.badge) {
					AttemptItemBadge.UNREPORTED -> {
						Row(
							modifier = Modifier.padding(start = 8.dp),
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							AttemptStatusChip(
								modifier = Modifier.testTag(RecordUiTags.attemptStatusChip(item.attemptId)),
								text = stringResource(Res.string.attempt_unreported)
							)
							if (display.gradeText.isNotBlank()) {
								Text(
									text = display.gradeText,
									fontWeight = FontWeight.SemiBold,
									style = MaterialTheme.typography.titleMedium
								)
							}
						}
					}
					AttemptItemBadge.APPROVED -> {
						AttemptStatusChip(
							modifier = Modifier
								.padding(start = 8.dp)
								.testTag(RecordUiTags.attemptStatusChip(item.attemptId)),
							text = stringResource(Res.string.attempt_approved)
						)
					}
					AttemptItemBadge.FAILED -> {
						AttemptStatusChip(
							modifier = Modifier
								.padding(start = 8.dp)
								.testTag(RecordUiTags.attemptStatusChip(item.attemptId)),
							text = stringResource(Res.string.attempt_failed)
						)
					}
					AttemptItemBadge.RETIRED -> {
						AttemptStatusChip(
							modifier = Modifier
								.padding(start = 8.dp)
								.testTag(RecordUiTags.attemptStatusChip(item.attemptId)),
							text = stringResource(Res.string.attempt_retired)
						)
					}
					AttemptItemBadge.WITHOUT_EFFECT -> {
						Row(
							modifier = Modifier.padding(start = 8.dp),
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							AttemptStatusChip(
								modifier = Modifier.testTag(RecordUiTags.attemptStatusChip(item.attemptId)),
								text = stringResource(Res.string.attempt_without_effect)
							)
							if (display.gradeText.isNotBlank()) {
								Text(
									text = display.gradeText,
									fontWeight = FontWeight.SemiBold,
									style = MaterialTheme.typography.titleMedium
								)
							}
						}
					}
					null -> {
						if (display.gradeText.isNotBlank()) {
							Text(
								text = display.gradeText,
								fontWeight = FontWeight.SemiBold,
								style = MaterialTheme.typography.titleMedium
							)
						}
					}
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

		if (!item.isReadOnly && !isQualitative) {
			Slider(
				modifier = Modifier
					.testTag(RecordUiTags.attemptGradeSlider(item.attemptId))
					.fillMaxWidth()
					.padding(top = 8.dp),
				value = currentGrade.toFloat(),
				steps = MAX_SUBJECT_GRADE - 1,
				valueRange = Ranges.subjectGrade,
				onValueChange = { value ->
					val newGrade = value.roundToInt()

					if (newGrade != currentGrade) {
						gradeState?.intValue = newGrade
						onGradeChange(newGrade, null, false)
					}
				},
				onValueChangeFinished = {
					onGradeChange(gradeState?.intValue ?: currentGrade, null, true)
				}
			)
		}

		if (!item.isReadOnly && isQualitative) {
			val selectedOutcome = item.outcome

			FlowRow(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				QualitativeStatusOption(
					attemptId = item.attemptId,
					label = stringResource(Res.string.attempt_approved),
					outcome = AttemptOutcome.APPROVED,
					selectedOutcome = selectedOutcome,
					onSelected = { outcome -> onGradeChange(null, outcome, true) }
				)
				QualitativeStatusOption(
					attemptId = item.attemptId,
					label = stringResource(Res.string.attempt_failed),
					outcome = AttemptOutcome.FAILED,
					selectedOutcome = selectedOutcome,
					onSelected = { outcome -> onGradeChange(null, outcome, true) }
				)
				QualitativeStatusOption(
					attemptId = item.attemptId,
					label = stringResource(Res.string.attempt_retired),
					outcome = AttemptOutcome.RETIRED,
					selectedOutcome = selectedOutcome,
					onSelected = { outcome -> onGradeChange(null, outcome, true) }
				)
			}
		}
	}
}

@Composable
private fun QualitativeStatusOption(
	attemptId: String,
	label: String,
	outcome: AttemptOutcome,
	selectedOutcome: AttemptOutcome?,
	onSelected: (AttemptOutcome) -> Unit
) {
	FilterChip(
		modifier = Modifier.testTag(RecordUiTags.attemptStatusOption(attemptId, outcome.name.lowercase())),
		selected = outcome == selectedOutcome,
		onClick = { onSelected(outcome) },
		label = { Text(text = label) },
		colors = FilterChipDefaults.filterChipColors()
	)
}

@Composable
private fun AttemptStatusChip(
	modifier: Modifier = Modifier,
	text: String
) {
	Text(
		modifier = modifier
			.background(
				color = MaterialTheme.colorScheme.surfaceVariant,
				shape = RoundedCornerShape(8.dp)
			)
			.padding(vertical = 4.dp, horizontal = 10.dp),
		text = text,
		style = MaterialTheme.typography.labelLarge
	)
}
