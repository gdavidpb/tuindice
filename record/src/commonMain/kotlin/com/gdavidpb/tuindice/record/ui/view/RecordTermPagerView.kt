package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.NoticeView
import com.gdavidpb.tuindice.base.utils.extension.rememberLastNonNull
import com.gdavidpb.tuindice.record.presentation.model.RecordNotice
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter

@Composable
fun RecordTermPagerView(
	modifier: Modifier = Modifier,
	terms: List<TermItem>,
	notice: RecordNotice? = null,
	selectedTermId: String,
	onSelectedTermChange: (termId: String) -> Unit,
	onAttemptSelectionChange: (
		attemptId: String,
		newGrade: Int?,
		newOutcome: AttemptOutcome?,
		isSelected: Boolean
	) -> Unit,
	onScrollInProgressChange: (Boolean) -> Unit
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
	val listScrollInProgress = remember { mutableStateOf(false) }

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

	LaunchedEffect(pagerState, listScrollInProgress) {
		snapshotFlow { pagerState.isScrollInProgress || listScrollInProgress.value }
			.distinctUntilChanged()
			.collect(onScrollInProgressChange)
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

		// The notice every page shares (a final annulment has no current term to sit on). Always
		// composed, so it animates in and out instead of making the pages jump.
		val shownNotice = rememberLastNonNull(notice)

		NoticeView(
			modifier = Modifier.padding(vertical = TuIndiceSpacing.Medium),
			visible = notice != null,
			title = shownNotice?.title?.asString(),
			message = shownNotice?.message?.asString().orEmpty(),
			// The fallback is never drawn: nothing shows until a notice has brought its own icon.
			icon = shownNotice?.icon ?: Icons.Outlined.Info
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
				onAttemptSelectionChange = onAttemptSelectionChange,
				onScrollInProgressChange = { isScrollInProgress ->
					listScrollInProgress.value = isScrollInProgress
				}
			)
		}
	}
}
