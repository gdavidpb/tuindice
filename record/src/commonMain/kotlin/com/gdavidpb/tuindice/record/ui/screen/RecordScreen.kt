package com.gdavidpb.tuindice.record.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.academiccore.domain.model.isSynthetic
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.domain.model.filteredProjectionFor
import com.gdavidpb.tuindice.record.presentation.contract.Record
import com.gdavidpb.tuindice.record.ui.RecordUiTags
import com.gdavidpb.tuindice.record.ui.view.RecordContentView
import com.gdavidpb.tuindice.record.ui.view.RecordEmptyView
import com.gdavidpb.tuindice.record.ui.view.RecordFailedView
import com.gdavidpb.tuindice.record.ui.view.RecordLoadingView
import com.gdavidpb.tuindice.record.ui.view.RecordSyntheticTermActionsView
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
	) -> Unit,
	onCreateSyntheticTermClick: () -> Unit,
	onUpdateSyntheticTermClick: (termId: String) -> Unit = {},
	onDeleteSyntheticTermClick: (termId: String) -> Unit = {}
) {
	val contentScrollInProgress = remember { mutableStateOf(false) }
	val contentState = state as? Record.State.Content
	val selectedTermIdValue = selectedTermId ?: contentState?.selectedTermId
	val selectedSyntheticTerm = contentState
		?.takeIf { content -> content.viewMode == RecordViewMode.Projection }
		?.record
		?.filteredProjectionFor(RecordViewMode.Projection)
		?.terms
		?.firstOrNull { term ->
			term.id == selectedTermIdValue && term.kind.isSynthetic
		}

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
						onAttemptSelectionChange = onAttemptSelectionChange,
						onScrollInProgressChange = { isScrollInProgress ->
							contentScrollInProgress.value = isScrollInProgress
						}
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

		AnimatedVisibility(
			modifier = Modifier
				.align(Alignment.BottomEnd)
				.padding(24.dp),
			visible = state is Record.State.Content &&
				state.viewMode == RecordViewMode.Projection &&
				!contentScrollInProgress.value,
			enter = fadeIn(),
			exit = fadeOut()
		) {
			Column(horizontalAlignment = Alignment.End) {
				selectedSyntheticTerm?.let { term ->
					RecordSyntheticTermActionsView(
						termId = term.id,
						onEditClick = onUpdateSyntheticTermClick,
						onDeleteClick = onDeleteSyntheticTermClick
					)
				}

				FloatingActionButton(
					modifier = Modifier.testTag(RecordUiTags.CreateSyntheticTermFab),
					containerColor = MaterialTheme.colorScheme.primary,
					onClick = onCreateSyntheticTermClick
				) {
					Icon(
						imageVector = Icons.Outlined.Add,
						contentDescription = null
					)
				}
			}
		}
	}
}
