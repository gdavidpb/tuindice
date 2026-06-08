package com.gdavidpb.tuindice.pensum.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gdavidpb.tuindice.base.ui.dialog.ConfirmationDialog
import com.gdavidpb.tuindice.pensum.presentation.model.PensumNodeItem
import com.gdavidpb.tuindice.pensum.ui.PensumUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_subject_detail_close
import tuindice.pensum.generated.resources.pensum_subject_detail_stats
import tuindice.pensum.generated.resources.pensum_subject_detail_stats_unavailable
import tuindice.pensum.generated.resources.pensum_subject_detail_title

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PensumSubjectDetailBottomSheet(
	node: PensumNodeItem,
	shouldStartExpanded: Boolean,
	onSubjectStatsClick: (subjectCode: String) -> Unit,
	onRelatedSubjectClick: (nodeId: String) -> Unit,
	onDismissRequest: () -> Unit
) {
	val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
	val detail = node.detail
	val statsCode = detail.statsCode
	val hasMoreDetail =
		detail.fulfilledSubject != null ||
			detail.blockingReasons.isNotEmpty() ||
			detail.requirements.isNotEmpty() ||
			detail.corequisites.isNotEmpty() ||
			detail.unlocks.isNotEmpty()
	val shouldStartWithMoreDetailExpanded = shouldStartExpanded && hasMoreDetail
	val isMoreDetailExpandedState = remember(node.id, shouldStartWithMoreDetailExpanded) {
		mutableStateOf(shouldStartWithMoreDetailExpanded)
	}

	LaunchedEffect(isMoreDetailExpandedState.value) {
		if (isMoreDetailExpandedState.value) {
			sheetState.expand()
		}
	}

	ConfirmationDialog(
		sheetState = sheetState,
		titleText = stringResource(Res.string.pensum_subject_detail_title),
		positiveText = statsCode?.let { stringResource(Res.string.pensum_subject_detail_stats) },
		negativeText = stringResource(Res.string.pensum_subject_detail_close),
		onPositiveClick = {
			if (statsCode != null) {
				onSubjectStatsClick(statsCode)
			}
		},
		onDismissRequest = onDismissRequest
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.verticalScroll(rememberScrollState())
				.testTag(PensumUiTags.SubjectDetailSheet)
				.animateContentSize(
					animationSpec = tween(
						durationMillis = SubjectDetailExpansionDurationMillis,
						easing = FastOutSlowInEasing
					)
				),
			verticalArrangement = Arrangement.spacedBy(14.dp)
		) {
			PensumSubjectOverviewCard(node = node)

			if (statsCode == null) {
				Text(
					modifier = Modifier.testTag(PensumUiTags.SubjectDetailStatsUnavailable),
					text = stringResource(Res.string.pensum_subject_detail_stats_unavailable),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}

			AnimatedVisibility(
				visible = !isMoreDetailExpandedState.value && hasMoreDetail,
				enter = fadeIn(
					animationSpec = tween(durationMillis = SubjectDetailFadeDurationMillis)
				) + expandVertically(
					animationSpec = tween(
						durationMillis = SubjectDetailExpansionDurationMillis,
						easing = FastOutSlowInEasing
					),
					expandFrom = Alignment.Top
				),
				exit = fadeOut(
					animationSpec = tween(durationMillis = SubjectDetailFadeDurationMillis)
				) + shrinkVertically(
					animationSpec = tween(
						durationMillis = SubjectDetailFadeDurationMillis,
						easing = FastOutSlowInEasing
					),
					shrinkTowards = Alignment.Top
				)
			) {
				PensumSubjectMoreDetailButton(
					onClick = { isMoreDetailExpandedState.value = true }
				)
			}

			AnimatedVisibility(
				visible = isMoreDetailExpandedState.value,
				enter = fadeIn(
					animationSpec = tween(
						durationMillis = SubjectDetailFadeDurationMillis,
						delayMillis = SubjectDetailFadeDelayMillis
					)
				) + expandVertically(
					animationSpec = tween(
						durationMillis = SubjectDetailExpansionDurationMillis,
						easing = FastOutSlowInEasing
					),
					expandFrom = Alignment.Top
				),
				exit = fadeOut(
					animationSpec = tween(durationMillis = SubjectDetailFadeDurationMillis)
				) + shrinkVertically(
					animationSpec = tween(
						durationMillis = SubjectDetailFadeDurationMillis,
						easing = FastOutSlowInEasing
					),
					shrinkTowards = Alignment.Top
				)
			) {
				PensumSubjectExpandedDetailContent(
					node = node,
					onRelatedSubjectClick = onRelatedSubjectClick
				)
			}
		}
	}
}
