package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.GradingMode
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.model.AttemptArrivalHighlight
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun SelectedTermView(
	modifier: Modifier = Modifier,
	term: TermItem,
	onArrivalTaken: (attemptId: String) -> Unit = {},
	onAttemptSelectionChange: (
		attemptId: String,
		newGrade: Int?,
		newOutcome: AttemptOutcome?,
		isSelected: Boolean
	) -> Unit,
	onScrollInProgressChange: (Boolean) -> Unit = {}
) {
	val lazyListState = rememberLazyListState()
	val gradeStates = remember(term.termId, term.attempts.map { attempt -> attempt.attemptId to attempt.grade }) {
		HashMap(term.attempts.associate { attempt -> attempt.attemptId to mutableIntStateOf(attempt.grade) })
	}
	val highlighted = rememberAttemptArrival(
		term = term,
		lazyListState = lazyListState,
		onArrivalTaken = onArrivalTaken
	)

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
		contentPadding = PaddingValues(bottom = SelectedTermListBottomPadding)
	) {
		items(
			items = term.attempts,
			key = { attempt -> attempt.attemptId },
			contentType = { SelectedTermAttemptContentType }
		) { attempt ->
			val gradeState = gradeStates.getOrPut(attempt.attemptId) {
				mutableIntStateOf(attempt.grade)
			}

			AttemptCardItemView(
				item = attempt,
				gradeState = gradeState.takeIf { attempt.gradingMode == GradingMode.NUMERIC },
				onGradeChange = { newGrade, newOutcome, isSelected ->
					onAttemptSelectionChange(attempt.attemptId, newGrade, newOutcome, isSelected)
				},
				arrivalHighlight = AttemptArrivalHighlights(highlighted).of(attempt.attemptId)
			)
		}
	}
}

// The arrival of a jump into this term. The term carries it while it is pending and the content forgets it as soon as
// it is taken, so the list keeps it here: its scroll and its highlight are not cut when the content lets go.
// The term opens at its first card, or at the card of the arrival, which then plays its highlight. One effect for
// both, keyed by the arrival too, so an opening can never land after the scroll to the arrival.
@Composable
private fun rememberAttemptArrival(
	term: TermItem,
	lazyListState: LazyListState,
	onArrivalTaken: (attemptId: String) -> Unit
): MutableState<AttemptArrival?> {
	val arrival = remember { mutableStateOf<AttemptArrival?>(null) }
	val highlighted = remember { mutableStateOf<AttemptArrival?>(null) }

	LaunchedEffect(term.arrivalAttemptId) {
		val attemptId = term.arrivalAttemptId ?: return@LaunchedEffect

		arrival.value = AttemptArrival(attemptId = attemptId)
		onArrivalTaken(attemptId)
	}

	LaunchedEffect(term.termId, arrival.value) {
		val take = arrival.value
		val index = term.attempts.indexOfFirst { attempt -> attempt.attemptId == take?.attemptId }

		lazyListState.scrollToItem(maxOf(index, 0))

		if (take != null && index >= 0) {
			highlighted.value = take
		}
	}

	return highlighted
}

// One arrival taken from the content. Each take is a new object, so a jump is told apart from the one before
// it even when both name the same attempt.
private class AttemptArrival(
	val attemptId: String
)

// The highlight of the card of the arrival while it plays. Its end clears the highlight, so the card is not
// highlighted again when it leaves the list and comes back.
private class AttemptArrivalHighlights(
	private val highlighted: MutableState<AttemptArrival?>
) {
	fun of(attemptId: String): AttemptArrivalHighlight? {
		val take = highlighted.value?.takeIf { arrival -> arrival.attemptId == attemptId } ?: return null

		return AttemptArrivalHighlight(
			trigger = take,
			onFinished = {
				if (highlighted.value === take) {
					highlighted.value = null
				}
			}
		)
	}
}

private val SelectedTermListBottomPadding = 120.dp
private const val SelectedTermAttemptContentType = "selected_term_attempt"
