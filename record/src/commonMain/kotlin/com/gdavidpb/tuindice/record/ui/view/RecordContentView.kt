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
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.ui.style.InternalScreenDefaults
import com.gdavidpb.tuindice.base.utils.extension.formatGrade
import com.gdavidpb.tuindice.record.domain.model.filteredProjectionFor
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.presentation.mapper.RecordMapperTexts
import com.gdavidpb.tuindice.record.presentation.mapper.toTermItemList
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.dialog.TermSelectionBottomSheet
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
		attemptId: String,
		newGrade: Int?,
		newOutcome: AttemptOutcome?,
		isSelected: Boolean
	) -> Unit,
	showTermSelection: Boolean,
	onDismissTermSelection: () -> Unit,
	onScrollInProgressChange: (Boolean) -> Unit = {}
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

	val terms = state.record.filteredProjectionFor(state.viewMode)
		.terms
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
				onAttemptSelectionChange = onAttemptSelectionChange,
				onScrollInProgressChange = onScrollInProgressChange
			)
		}
	}

	if (showTermSelection && terms.isNotEmpty() && effectiveSelectedTermId != null) {
		TermSelectionBottomSheet(
			terms = terms,
			selectedTermId = effectiveSelectedTermId,
			viewMode = state.viewMode,
			onTermSelected = onSelectedTermChange,
			onDismissRequest = onDismissTermSelection
		)
	}
}
