package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.RecordMapperTexts
import com.gdavidpb.tuindice.record.presentation.mapper.toQuarterItemList
import com.gdavidpb.tuindice.record.presentation.model.QuarterItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
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
	selectedQuarterId: String?,
	onSelectedQuarterChange: (quarterId: String) -> Unit,
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
	val effectiveSelectedQuarterId = selectedQuarterId ?: quarters.firstOrNull()?.quarterId

	Column(
		modifier = Modifier
			.fillMaxSize()
			.testTag(RecordUiTags.ContentContainer)
	) {
		if (
			chronologicalQuarters.isNotEmpty() &&
			(effectiveSelectedQuarterId != null)
		) {
			RecordQuarterPagerView(
				modifier = Modifier.fillMaxSize(),
				quarters = chronologicalQuarters,
				selectedQuarterId = effectiveSelectedQuarterId,
				onSelectedQuarterChange = onSelectedQuarterChange,
				onSubjectGradeChange = onSubjectGradeChange
			)
		}
	}
}

@Composable
private fun RecordQuarterPagerView(
	modifier: Modifier = Modifier,
	quarters: List<QuarterItem>,
	selectedQuarterId: String,
	onSelectedQuarterChange: (quarterId: String) -> Unit,
	onSubjectGradeChange: (
		quarterId: String,
		subjectId: String,
		newGrade: Int,
		isSelected: Boolean
	) -> Unit
) {
	val quarterIds = remember(quarters) {
		quarters.map { quarter -> quarter.quarterId }
	}
	val selectedQuarterIndex = remember(quarterIds, selectedQuarterId) {
		quarterIds.indexOf(selectedQuarterId).takeIf { index -> index >= 0 } ?: 0
	}
	val pagerState = rememberPagerState(
		initialPage = selectedQuarterIndex,
		pageCount = { quarters.size }
	)

	LaunchedEffect(quarterIds, selectedQuarterIndex) {
		if (pagerState.currentPage != selectedQuarterIndex) {
			pagerState.animateScrollToPage(selectedQuarterIndex)
		}
	}

	LaunchedEffect(pagerState, quarterIds, selectedQuarterId) {
		snapshotFlow { pagerState.isScrollInProgress }
			.filter { isScrollInProgress -> !isScrollInProgress }
			.distinctUntilChanged()
			.collect {
				val quarterId = quarterIds.getOrNull(pagerState.currentPage) ?: return@collect

				if (quarterId != selectedQuarterId) {
					onSelectedQuarterChange(quarterId)
				}
			}
	}

	Column(modifier = modifier) {
		QuarterSelectorView(
			modifier = Modifier.fillMaxWidth(),
			quarters = quarters,
			selectedQuarterId = quarterIds.getOrNull(pagerState.currentPage) ?: selectedQuarterId,
			onQuarterSelected = onSelectedQuarterChange
		)

		HorizontalPager(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f)
				.testTag(RecordUiTags.QuarterPager),
			state = pagerState,
			key = { page -> quarterIds[page] }
		) { page ->
			val quarter = quarters[page]

			Column(
				modifier = Modifier.fillMaxSize()
			) {
				QuarterSummaryView(
					modifier = Modifier
						.fillMaxWidth()
						.testTag(RecordUiTags.SelectedQuarterSummary),
					item = quarter
				)

				SelectedQuarterView(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f),
					quarter = quarter,
					onSubjectGradeChange = onSubjectGradeChange
				)
			}
		}
	}
}
