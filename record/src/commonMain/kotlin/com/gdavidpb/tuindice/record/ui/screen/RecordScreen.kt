package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.runtime.Composable
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
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
	selectedQuarterId: String?,
	onViewModeChange: (viewMode: RecordViewMode) -> Unit,
	onSelectedQuarterChange: (quarterId: String) -> Unit,
	onRetryClick: () -> Unit,
	onSubjectGradeChange: (
		quarterId: String,
		subjectId: String,
		newGrade: Int,
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
					selectedQuarterId = selectedQuarterId,
					onViewModeChange = onViewModeChange,
					onSelectedQuarterChange = onSelectedQuarterChange,
					onSubjectGradeChange = onSubjectGradeChange
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
