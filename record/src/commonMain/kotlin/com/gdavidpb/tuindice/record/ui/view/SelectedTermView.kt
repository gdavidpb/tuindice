package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.domain.model.GradingMode
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun SelectedTermView(
	modifier: Modifier = Modifier,
	term: TermItem,
	onAttemptSelectionChange: (
		attemptId: String,
		newGrade: Int?,
		newOutcome: AttemptOutcome?,
		isSelected: Boolean
	) -> Unit,
	onScrollInProgressChange: (Boolean) -> Unit = {}
) {
	val lazyListState = rememberLazyListState()
	val gradeStates = remember(
		term.termId,
		term.attempts.map { attempt -> attempt.attemptId to attempt.grade }
	) {
		HashMap(
			term.attempts.associate { attempt ->
				attempt.attemptId to mutableIntStateOf(attempt.grade)
			}
		)
	}

	LaunchedEffect(term.termId) {
		lazyListState.scrollToItem(0)
	}

	LaunchedEffect(lazyListState) {
		snapshotFlow { lazyListState.isScrollInProgress }
			.distinctUntilChanged()
			.collect(onScrollInProgressChange)
	}

	DisposableEffect(Unit) {
		onDispose {
			onScrollInProgressChange(false)
		}
	}

	LazyColumn(
		modifier = modifier.testTag(RecordUiTags.AttemptsList),
		state = lazyListState,
		contentPadding = PaddingValues(bottom = 16.dp)
	) {
		items(
			items = term.attempts,
			key = { attempt -> attempt.attemptId }
		) { attempt ->
			val gradeState = gradeStates.getOrPut(attempt.attemptId) {
				mutableIntStateOf(attempt.grade)
			}

			AttemptCardItemView(
				item = attempt,
				gradeState = gradeState.takeIf { attempt.gradingMode == GradingMode.NUMERIC },
				onGradeChange = { newGrade, newOutcome, isSelected ->
					onAttemptSelectionChange(
						attempt.attemptId,
						newGrade,
						newOutcome,
						isSelected
					)
				}
			)
		}
	}
}
