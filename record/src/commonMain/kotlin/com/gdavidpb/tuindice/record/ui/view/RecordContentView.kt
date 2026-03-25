package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.toQuarterItemList
import com.gdavidpb.tuindice.record.presentation.model.RecordMapperTexts
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.quarter_credits_pattern
import tuindice.record.generated.resources.quarter_grade_diff_pattern
import tuindice.record.generated.resources.quarter_grade_sum_pattern
import tuindice.record.generated.resources.subject_credits_pattern
import tuindice.record.generated.resources.subject_grade_pattern

@Composable
fun RecordContentView(
	state: Record.State.Content,
	onSubjectGradeChange: (
		quarterId: String,
		subjectId: String,
		newGrade: Int,
		isSelected: Boolean
	) -> Unit
) {
	val quarterGradeDiffPattern = stringResource(Res.string.quarter_grade_diff_pattern)
	val quarterGradeSumPattern = stringResource(Res.string.quarter_grade_sum_pattern)
	val quarterCreditsPattern = stringResource(Res.string.quarter_credits_pattern)
	val subjectGradePattern = stringResource(Res.string.subject_grade_pattern)
	val subjectCreditsPattern = stringResource(Res.string.subject_credits_pattern)

	val texts = remember(
		quarterGradeDiffPattern,
		quarterGradeSumPattern,
		quarterCreditsPattern,
		subjectGradePattern,
		subjectCreditsPattern
	) {
		RecordMapperTexts(
			quarterGradeDiff = { grade ->
				quarterGradeDiffPattern.replace("%1${'$'}.4f", grade.formatGrade(decimals = 4))
			},
			quarterGradeSum = { grade ->
				quarterGradeSumPattern.replace("%1${'$'}.4f", grade.formatGrade(decimals = 4))
			},
			quarterCredits = { credits ->
				quarterCreditsPattern.replace("%1${'$'}d", credits.toString())
			},
			subjectGrade = { grade ->
				subjectGradePattern.replace("%1${'$'}d", grade.toString())
			},
			subjectCredits = { credits ->
				subjectCreditsPattern.replace("%1${'$'}d", credits.toString())
			}
		)
	}

	val quarters = state
		.quarters
		.toQuarterItemList(
			texts = texts,
			highlightColor = MaterialTheme.colorScheme.primary
		)
	val chronologicalQuarters = quarters.asReversed()
	val selectedQuarterIdState = remember {
		mutableStateOf(quarters.firstOrNull()?.quarterId)
	}

	LaunchedEffect(quarters.map { quarter -> quarter.quarterId }) {
		selectedQuarterIdState.value = when {
			quarters.isEmpty() -> null
			quarters.any { quarter -> quarter.quarterId == selectedQuarterIdState.value } ->
				selectedQuarterIdState.value
			else -> quarters.first().quarterId
		}
	}

	val selectedQuarter = quarters.firstOrNull { quarter ->
		quarter.quarterId == selectedQuarterIdState.value
	}

	Column(
		modifier = Modifier
			.fillMaxSize()
			.testTag(RecordUiTags.ContentContainer)
	) {
		if (chronologicalQuarters.isNotEmpty()) {
			QuarterSelectorView(
				modifier = Modifier.fillMaxWidth(),
				quarters = chronologicalQuarters,
				selectedQuarterId = selectedQuarter?.quarterId,
				onQuarterSelected = { quarterId ->
					selectedQuarterIdState.value = quarterId
				}
			)
		}

		if (selectedQuarter != null) {
			QuarterSummaryView(
				modifier = Modifier
					.fillMaxWidth()
					.testTag(RecordUiTags.SelectedQuarterSummary),
				item = selectedQuarter,
				showTitle = false,
				elevated = false
			)

			SelectedQuarterView(
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f),
				quarter = selectedQuarter,
				onSubjectGradeChange = onSubjectGradeChange
			)
		}
	}
}
