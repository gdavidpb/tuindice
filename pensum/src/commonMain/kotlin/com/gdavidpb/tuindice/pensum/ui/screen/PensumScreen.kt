package com.gdavidpb.tuindice.pensum.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.view.ErrorStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.ErrorView
import com.gdavidpb.tuindice.base.ui.view.SealedCrossfade
import com.gdavidpb.tuindice.pensum.presentation.contract.Pensum
import com.gdavidpb.tuindice.pensum.presentation.model.PensumModalityItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumOptionItem
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import com.gdavidpb.tuindice.pensum.ui.view.PensumContentView
import com.gdavidpb.tuindice.pensum.ui.view.PensumEmptyView
import com.gdavidpb.tuindice.pensum.ui.view.PensumLoadingView
import com.gdavidpb.tuindice.pensum.ui.view.pensumGraphColors
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_empty_message
import tuindice.pensum.generated.resources.pensum_empty_title
import tuindice.pensum.generated.resources.pensum_failed_retry
import tuindice.pensum.generated.resources.pensum_failed_title
import tuindice.pensum.generated.resources.pensum_record_unavailable_message
import tuindice.pensum.generated.resources.pensum_record_unavailable_title

@Composable
fun PensumScreen(
	state: Pensum.State,
	onRetryClick: () -> Unit,
	showSelectionSheet: Boolean,
	onSummaryCollapsedToggle: () -> Unit = {},
	onSelectionSheetDismiss: () -> Unit,
	onSubjectStatsClick: (subjectCode: String) -> Unit,
	onSelectionApplied: (PensumOptionItem, PensumModalityItem) -> Unit,
	onPensumContextClick: () -> Unit = {}
) {
	val graphColors = pensumGraphColors()

	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(graphColors.screenBackground)
	) {
		CompositionLocalProvider(LocalContentColor provides graphColors.textPrimary) {
			SealedCrossfade(targetState = state) { targetState ->
				when (targetState) {
					is Pensum.State.Idle -> Unit
					is Pensum.State.Loading -> PensumLoadingView()
					is Pensum.State.Empty -> PensumEmptyView(
						title = stringResource(Res.string.pensum_empty_title),
						message = stringResource(Res.string.pensum_empty_message)
					)
					is Pensum.State.RecordDataUnavailable -> PensumEmptyView(
						title = stringResource(Res.string.pensum_record_unavailable_title),
						message = stringResource(Res.string.pensum_record_unavailable_message),
						actionLabel = stringResource(Res.string.pensum_failed_retry),
						onActionClick = onRetryClick
					)
					is Pensum.State.Content -> PensumContentView(
						model = targetState.model,
						isRefreshing = targetState.isRefreshing,
						localDataMessage = targetState.localDataMessage,
						showSelectionSheet = showSelectionSheet,
						isSummaryCollapsed = targetState.isSummaryCollapsed,
						onSummaryCollapsedToggle = onSummaryCollapsedToggle,
						onSelectionSheetDismiss = onSelectionSheetDismiss,
						onSubjectStatsClick = onSubjectStatsClick,
						onSelectionApplied = onSelectionApplied,
						onPensumContextClick = onPensumContextClick
					)
					is Pensum.State.Failed ->
						ErrorView(
							title = stringResource(Res.string.pensum_failed_title),
							message = targetState.message.asString(),
							retryText = stringResource(Res.string.pensum_failed_retry),
							onRetryClick = onRetryClick,
							headerContent = { ErrorStateAnimationView() }
						)
				}
			}
		}
	}
}
