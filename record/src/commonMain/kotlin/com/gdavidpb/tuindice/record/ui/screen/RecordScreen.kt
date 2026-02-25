package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.record.presentation.contract.Record

@Composable
fun RecordScreen(
	state: Record.State,
	onRetryClick: () -> Unit,
	onSubjectGradeChange: (
		quarterId: String,
		subjectId: String,
		newGrade: Int,
		isSelected: Boolean
	) -> Unit,
	loadingContent: @Composable () -> Unit,
	contentStateContent: @Composable (
		state: Record.State.Content,
		onSubjectGradeChange: (
			quarterId: String,
			subjectId: String,
			newGrade: Int,
			isSelected: Boolean
		) -> Unit
	) -> Unit,
	failedContent: @Composable (onRetryClick: () -> Unit) -> Unit,
	emptyContent: @Composable () -> Unit
) {
	SealedCrossfade(
		targetState = state
	) { targetState ->
		when (targetState) {
			is Record.State.Loading ->
				loadingContent()

			is Record.State.Content ->
				contentStateContent(targetState, onSubjectGradeChange)

			is Record.State.Failed ->
				failedContent(onRetryClick)

			is Record.State.Empty ->
				emptyContent()
		}
	}
}
