package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.ui.view.RecordContentView
import com.gdavidpb.tuindice.record.ui.view.RecordEmptyView
import com.gdavidpb.tuindice.record.ui.view.RecordFailedView
import com.gdavidpb.tuindice.record.ui.view.RecordLoadingView
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.record_app_name
import tuindice.record.generated.resources.record_app_uni
import tuindice.record.generated.resources.record_empty_illustration_message
import tuindice.record.generated.resources.record_failed_message
import tuindice.record.generated.resources.record_failed_retry
import tuindice.record.generated.resources.record_failed_title

@Composable
fun RecordScreen(
	state: Record.State,
	selectedTermId: String?,
	onSelectedTermChange: (termId: String) -> Unit,
	onRetryClick: () -> Unit,
	onAttemptSelectionChange: (
		termId: String,
		attemptId: String,
		newGrade: Int,
		isSelected: Boolean
	) -> Unit
) {
	RecordScreen(
		state = state,
		selectedTermId = selectedTermId,
		onSelectedTermChange = onSelectedTermChange,
		onRetryClick = onRetryClick,
		onAttemptSelectionChange = { termId, attemptId, newGrade, _, isSelected ->
			onAttemptSelectionChange(termId, attemptId, newGrade ?: 0, isSelected)
		}
	)
}

@Composable
fun RecordScreen(
	state: Record.State,
	selectedTermId: String?,
	onSelectedTermChange: (termId: String) -> Unit,
	onRetryClick: () -> Unit,
	onAttemptSelectionChange: (
		termId: String,
		attemptId: String,
		newGrade: Int?,
		newStatus: SubjectStatus?,
		isSelected: Boolean
	) -> Unit
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
					selectedTermId = selectedTermId,
					onSelectedTermChange = onSelectedTermChange,
					onAttemptSelectionChange = onAttemptSelectionChange
				)

			is Record.State.Failed ->
				RecordFailedView(
					title = stringResource(Res.string.record_failed_title),
					message = stringResource(Res.string.record_failed_message),
					retryText = stringResource(Res.string.record_failed_retry),
					onRetryClick = onRetryClick,
					headerContent = {
						ErrorStateAnimationView()
					}
				)

			is Record.State.Empty ->
				RecordEmptyView(
					message = stringResource(Res.string.record_empty_illustration_message),
					highlightedParts = listOf(
						stringResource(Res.string.record_app_name),
						stringResource(Res.string.record_app_uni)
					)
				)
		}
	}
}
