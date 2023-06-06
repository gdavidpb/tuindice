package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.ui.view.RecordContentView
import com.gdavidpb.tuindice.record.ui.view.RecordEmptyView
import com.gdavidpb.tuindice.record.ui.view.RecordFailedView
import com.gdavidpb.tuindice.record.ui.view.RecordLoadingView

@Composable
fun RecordScreen(
	state: Record.State,
	onRetryClick: () -> Unit,
	onSubjectGradeChange: (subjectId: String, newGrade: Int, isSelected: Boolean) -> Unit
) {
	val previousState = remember { mutableStateOf(state) }

	Crossfade(
		targetState = state,
		animationSpec = if (previousState.value::class != state::class) tween() else snap()
	) { targetState ->
		previousState.value = targetState

		when (targetState) {
			is Record.State.Loading ->
				RecordLoadingView()

			is Record.State.Content ->
				RecordContentView(
					state = targetState,
					onSubjectGradeChange = onSubjectGradeChange
				)

			is Record.State.Failed ->
				RecordFailedView(
					onRetryClick = onRetryClick
				)

			is Record.State.Empty ->
				RecordEmptyView()
		}
	}
}