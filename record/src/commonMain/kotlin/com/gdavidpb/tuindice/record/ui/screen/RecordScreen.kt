package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.ui.view.RecordContentView
import com.gdavidpb.tuindice.record.ui.view.RecordEmptyView
import com.gdavidpb.tuindice.record.ui.view.RecordFailedView
import com.gdavidpb.tuindice.record.ui.view.RecordLoadingView
import org.jetbrains.compose.resources.stringResource
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.record_empty_message
import tuindice.record.generated.resources.record_empty_title
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
		attemptId: String,
		newGrade: Int?,
		newOutcome: AttemptOutcome?,
		isSelected: Boolean
	) -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		SealedCrossfade(
			targetState = state
		) { targetState ->
			when (targetState) {
				is Record.State.Idle -> Unit

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
						title = stringResource(Res.string.record_empty_title),
						message = stringResource(Res.string.record_empty_message)
					)
			}
		}
	}
}
