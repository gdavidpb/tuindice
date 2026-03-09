package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.RecordMapperTexts
import com.gdavidpb.tuindice.record.presentation.mapper.toQuarterItemList
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.quarter_credits_pattern
import tuindice.record.generated.resources.quarter_grade_diff_pattern
import tuindice.record.generated.resources.quarter_grade_sum_pattern
import tuindice.record.generated.resources.subject_credits_pattern
import tuindice.record.generated.resources.subject_grade_pattern
import tuindice.record.generated.resources.subject_retired
import tuindice.record.generated.resources.subject_status_pattern

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
	val subjectStatusPattern = stringResource(Res.string.subject_status_pattern)
	val subjectGradePattern = stringResource(Res.string.subject_grade_pattern)
	val subjectCreditsPattern = stringResource(Res.string.subject_credits_pattern)
	val subjectRetiredText = stringResource(Res.string.subject_retired)

	val texts = remember(
		quarterGradeDiffPattern,
		quarterGradeSumPattern,
		quarterCreditsPattern,
		subjectStatusPattern,
		subjectGradePattern,
		subjectCreditsPattern,
		subjectRetiredText
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
			subjectRetired = subjectRetiredText,
			subjectStatus = { status ->
				subjectStatusPattern.replace("%1${'$'}s", status)
			},
			subjectGrade = { grade ->
				subjectGradePattern.replace("%1${'$'}d", grade.toString())
			},
			subjectCredits = { credits ->
				subjectCreditsPattern.replace("%1${'$'}d", credits.toString())
			}
		)
	}

	val lazyColumState = rememberLazyListState()
	val quarters = state
		.quarters
		.toQuarterItemList(
			texts = texts,
			highlightColor = MaterialTheme.colorScheme.primary
		)

	QuartersView(
		modifier = Modifier.testTag(RecordUiTags.ContentContainer),
		lazyListState = lazyColumState,
		quarters = quarters,
		onSubjectGradeChange = onSubjectGradeChange
	)
}
