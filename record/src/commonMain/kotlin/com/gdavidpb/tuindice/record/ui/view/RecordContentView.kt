package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.engine.RecordProjectionEngine
import com.gdavidpb.tuindice.academiccore.domain.model.RecordProjection
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.filterByViewMode
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.RecordMapperTexts
import com.gdavidpb.tuindice.record.presentation.mapper.toTermItemList
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.term_attempt_credits_pattern
import tuindice.record.generated.resources.term_attempt_grade_pattern
import tuindice.record.generated.resources.term_credits_pattern
import tuindice.record.generated.resources.term_grade_diff_pattern
import tuindice.record.generated.resources.term_grade_sum_pattern

@Composable
fun RecordContentView(
	state: Record.State.Content,
	selectedTermId: String?,
	onSelectedTermChange: (termId: String) -> Unit,
	onAttemptSelectionChange: (
		termId: String,
		attemptId: String,
		newGrade: Int?,
		newStatus: SubjectStatus?,
		isSelected: Boolean
	) -> Unit
) {
	val termGradeDiffPattern = stringResource(Res.string.term_grade_diff_pattern)
	val termGradeSumPattern = stringResource(Res.string.term_grade_sum_pattern)
	val termCreditsPattern = stringResource(Res.string.term_credits_pattern)
	val attemptGradePattern = stringResource(Res.string.term_attempt_grade_pattern)
	val attemptCreditsPattern = stringResource(Res.string.term_attempt_credits_pattern)

	val texts = remember(
		termGradeDiffPattern,
		termGradeSumPattern,
		termCreditsPattern,
		attemptGradePattern,
		attemptCreditsPattern
	) {
		RecordMapperTexts(
			termGrade = { grade ->
				termGradeDiffPattern.replace("%1${'$'}.4f", grade.formatGrade(decimals = 4))
			},
			termGradeSum = { grade ->
				termGradeSumPattern.replace("%1${'$'}.4f", grade.formatGrade(decimals = 4))
			},
			termCredits = { credits ->
				termCreditsPattern.replace("%1${'$'}d", credits.toString())
			},
			termAttemptGrade = { grade ->
				attemptGradePattern.replace("%1${'$'}d", grade.toString())
			},
			termAttemptCredits = { credits ->
				attemptCreditsPattern.replace("%1${'$'}d", credits.toString())
			}
		)
	}

	val activeProjection = state.record.activeProjection(state.viewMode)
	val terms = activeProjection.terms
		.filterByViewMode(state.viewMode)
		.toTermItemList(
			viewMode = state.viewMode,
			texts = texts,
			highlightColor = MaterialTheme.colorScheme.primary
		)
		.asReversed()
	val effectiveSelectedTermId = selectedTermId ?: terms.firstOrNull()?.termId

	Column(
		modifier = Modifier
			.fillMaxSize()
			.testTag(RecordUiTags.ContentContainer)
	) {
		if (terms.isNotEmpty() && (effectiveSelectedTermId != null)) {
			RecordTermPagerView(
				modifier = Modifier
					.fillMaxSize()
					.padding(top = InternalScreenDefaults.TopBarSpacing),
				terms = terms,
				selectedTermId = effectiveSelectedTermId,
				onSelectedTermChange = onSelectedTermChange,
				onAttemptSelectionChange = onAttemptSelectionChange
			)
		}
	}
}

@Composable
private fun RecordTermPagerView(
	modifier: Modifier = Modifier,
	terms: List<TermItem>,
	selectedTermId: String,
	onSelectedTermChange: (termId: String) -> Unit,
	onAttemptSelectionChange: (
		termId: String,
		attemptId: String,
		newGrade: Int?,
		newStatus: SubjectStatus?,
		isSelected: Boolean
	) -> Unit
) {
	val termIds = remember(terms) {
		terms.map { term -> term.termId }
	}
	val selectedTermIndex = remember(termIds, selectedTermId) {
		termIds.indexOf(selectedTermId).takeIf { index -> index >= 0 } ?: 0
	}
	val pagerState = rememberPagerState(
		initialPage = selectedTermIndex,
		pageCount = { terms.size }
	)

	LaunchedEffect(termIds, selectedTermIndex) {
		if (pagerState.currentPage != selectedTermIndex) {
			pagerState.animateScrollToPage(selectedTermIndex)
		}
	}

	LaunchedEffect(pagerState, termIds, selectedTermId) {
		snapshotFlow { pagerState.isScrollInProgress to pagerState.currentPage }
			.distinctUntilChanged()
			.filter { (isScrollInProgress, _) -> !isScrollInProgress }
			.collect { (_, page) ->
				val termId = termIds.getOrNull(page) ?: return@collect

				if (termId != selectedTermId) {
					onSelectedTermChange(termId)
				}
			}
	}

	Column(modifier = modifier) {
		TermSelectorView(
			modifier = Modifier
				.fillMaxWidth()
				.padding(
					top = 8.dp,
					bottom = 8.dp
				),
			terms = terms,
			selectedTermId = termIds.getOrNull(pagerState.currentPage) ?: selectedTermId,
			onTermSelected = onSelectedTermChange
		)

		HorizontalPager(
			modifier = Modifier
				.fillMaxWidth()
				.weight(1f)
				.testTag(RecordUiTags.TermPager),
			state = pagerState,
			key = { page -> termIds[page] }
		) { page ->
			TermItemView(
				modifier = Modifier.fillMaxSize(),
				item = terms[page],
				onAttemptSelectionChange = onAttemptSelectionChange
			)
		}
	}
}

private fun com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord.activeProjection(
	viewMode: RecordViewMode
): RecordProjection {
	return when (viewMode) {
		RecordViewMode.Official -> RecordProjectionEngine.projectOfficial(this)
		RecordViewMode.Working -> RecordProjectionEngine.projectWorking(this)
	}
}
