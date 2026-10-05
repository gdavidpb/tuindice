package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.NoticeView
import com.gdavidpb.tuindice.record.presentation.model.RecordNotice
import com.gdavidpb.tuindice.record.presentation.model.RecordNoticeKind
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags

@Composable
fun TermItemView(
	modifier: Modifier = Modifier,
	item: TermItem,
	notice: RecordNotice? = null,
	onAttemptSelectionChange: (
		attemptId: String,
		newGrade: Int?,
		newOutcome: AttemptOutcome?,
		isSelected: Boolean
	) -> Unit,
	onScrollInProgressChange: (Boolean) -> Unit = {}
) {
	val isScheduleSelected = rememberSaveable { mutableStateOf(false) }
	val schedule = item.schedule
	val selectedSchedule = schedule.takeIf { isScheduleSelected.value }

	Column(modifier = modifier.fillMaxSize()) {
		// Only the current term has a schedule; the switch sits right under the term selector.
		if (schedule != null) {
			TermViewSwitchView(
				modifier = Modifier.padding(
					horizontal = TuIndiceSpacing.Screen,
					vertical = TuIndiceSpacing.Medium
				),
				isScheduleSelected = selectedSchedule != null,
				onScheduleSelectedChange = { isScheduleSelected.value = it }
			)
		}

		// Provisional annulment and stale data describe the current term, so only its page shows them.
		if (item.isCurrent && notice != null) {
			NoticeView(
				modifier = Modifier.padding(top = TuIndiceSpacing.Medium),
				title = notice.title?.asString(),
				message = notice.message.asString(),
				icon = if (notice.kind == RecordNoticeKind.StaleEnrollment) {
					Icons.Outlined.Schedule
				} else {
					Icons.Outlined.Info
				}
			)
		}

		TermSummaryView(
			modifier = Modifier
				.fillMaxWidth()
				.testTag(RecordUiTags.SelectedTermSummary),
			item = item
		)

		if (selectedSchedule != null) {
			TermScheduleView(
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f),
				grid = selectedSchedule
			)
		} else {
			SelectedTermView(
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f),
				term = item,
				onAttemptSelectionChange = onAttemptSelectionChange,
				onScrollInProgressChange = onScrollInProgressChange
			)
		}
	}
}
