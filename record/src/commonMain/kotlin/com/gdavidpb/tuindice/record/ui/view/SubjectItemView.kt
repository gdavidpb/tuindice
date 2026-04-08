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
import com.gdavidpb.tuindice.base.domain.model.subject.GradingMode
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.persistence.utils.MAX_SUBJECT_GRADE
import com.gdavidpb.tuindice.record.presentation.model.SubjectItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.SubjectItemBadge
import com.gdavidpb.tuindice.record.ui.model.toDisplay
import com.gdavidpb.tuindice.record.utils.Ranges
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.subject_approved
import tuindice.record.generated.resources.subject_failed
import tuindice.record.generated.resources.subject_pending
import tuindice.record.generated.resources.subject_retired
import tuindice.record.generated.resources.subject_without_effect
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectItemView(
	modifier: Modifier = Modifier,
	item: SubjectItem,
	gradeState: MutableIntState? = null,
	onGradeChange: (newGrade: Int, isSelected: Boolean) -> Unit
) {
	SubjectItemView(
		modifier = modifier,
		item = item,
		gradeState = gradeState,
		onGradeChange = { newGrade, _, isSelected ->
			onGradeChange(newGrade ?: item.grade, isSelected)
		}
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectItemView(
	modifier: Modifier = Modifier,
	item: SubjectItem,
	gradeState: MutableIntState? = null,
	onGradeChange: (newGrade: Int?, newStatus: SubjectStatus?, isSelected: Boolean) -> Unit
) {
	val currentGrade = gradeState?.intValue ?: item.grade
	val display = remember(item, currentGrade) {
		item.toDisplay(currentGrade = currentGrade)
	}
	val isQualitative = item.gradingMode == GradingMode.QUALITATIVE_PASS_FAIL

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
				when (display.badge) {
					SubjectItemBadge.APPROVED -> {
						SubjectStatusChip(
							modifier = Modifier
								.padding(start = 8.dp)
								.testTag(RecordUiTags.subjectStatusChip(item.subjectId)),
							text = stringResource(Res.string.subject_approved)
						)
					}
					SubjectItemBadge.FAILED -> {
						SubjectStatusChip(
							modifier = Modifier
								.padding(start = 8.dp)
								.testTag(RecordUiTags.subjectStatusChip(item.subjectId)),
							text = stringResource(Res.string.subject_failed)
						)
					}
					SubjectItemBadge.RETIRED -> {
						SubjectStatusChip(
							modifier = Modifier
								.padding(start = 8.dp)
								.testTag(RecordUiTags.subjectStatusChip(item.subjectId)),
							text = stringResource(Res.string.subject_retired)
						)
					}
					SubjectItemBadge.WITHOUT_EFFECT -> {
						Row(
							modifier = Modifier.padding(start = 8.dp),
							horizontalArrangement = Arrangement.spacedBy(8.dp),
							verticalAlignment = Alignment.CenterVertically
						) {
							SubjectStatusChip(
								modifier = Modifier.testTag(RecordUiTags.subjectStatusChip(item.subjectId)),
								text = stringResource(Res.string.subject_without_effect)
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
						onGradeChange(newGrade, null, false)
					}
				},
				onValueChangeFinished = {
					onGradeChange(gradeState?.intValue ?: currentGrade, null, true)
				}
			)
		}

		if (!item.isReadOnly && isQualitative) {
			val selectedStatus = item.status ?: SubjectStatus.NORMAL

			FlowRow(
				modifier = Modifier
					.fillMaxWidth()
					.padding(top = 8.dp),
				horizontalArrangement = Arrangement.spacedBy(8.dp),
				verticalArrangement = Arrangement.spacedBy(8.dp)
			) {
				QualitativeStatusOption(
					subjectId = item.subjectId,
					label = stringResource(Res.string.subject_pending),
					status = SubjectStatus.NORMAL,
					selectedStatus = selectedStatus,
					onSelected = { status -> onGradeChange(null, status, true) }
				)
				QualitativeStatusOption(
					subjectId = item.subjectId,
					label = stringResource(Res.string.subject_approved),
					status = SubjectStatus.APPROVED,
					selectedStatus = selectedStatus,
					onSelected = { status -> onGradeChange(null, status, true) }
				)
				QualitativeStatusOption(
					subjectId = item.subjectId,
					label = stringResource(Res.string.subject_failed),
					status = SubjectStatus.FAILED,
					selectedStatus = selectedStatus,
					onSelected = { status -> onGradeChange(null, status, true) }
				)
				QualitativeStatusOption(
					subjectId = item.subjectId,
					label = stringResource(Res.string.subject_retired),
					status = SubjectStatus.RETIRED,
					selectedStatus = selectedStatus,
					onSelected = { status -> onGradeChange(null, status, true) }
				)
			}
		}
	}
}

@Composable
private fun QualitativeStatusOption(
	subjectId: String,
	label: String,
	status: SubjectStatus,
	selectedStatus: SubjectStatus,
	onSelected: (SubjectStatus) -> Unit
) {
	FilterChip(
		modifier = Modifier.testTag(RecordUiTags.subjectStatusOption(subjectId, status.value)),
		selected = status == selectedStatus,
		onClick = { onSelected(status) },
		label = { Text(text = label) },
		colors = FilterChipDefaults.filterChipColors()
	)
}

@Composable
private fun SubjectStatusChip(
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
