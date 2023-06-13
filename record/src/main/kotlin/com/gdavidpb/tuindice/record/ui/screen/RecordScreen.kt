package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
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
	SealedCrossfade(
		targetState = state
	) { targetState ->
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