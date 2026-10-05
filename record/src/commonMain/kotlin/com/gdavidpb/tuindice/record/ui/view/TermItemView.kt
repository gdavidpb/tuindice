package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.NoticeView
import com.gdavidpb.tuindice.base.ui.view.rememberLastNonNull
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
	Column(modifier = modifier.fillMaxSize()) {
		// Provisional annulment and stale data describe the current term, so only its page shows them.
		// Always composed, so it animates in and out instead of making the page jump.
		val pageNotice = notice?.takeIf { item.isCurrent }
		val shownNotice = rememberLastNonNull(pageNotice)

		NoticeView(
			modifier = Modifier.padding(vertical = TuIndiceSpacing.Medium),
			visible = pageNotice != null,
			title = shownNotice?.title?.asString(),
			message = shownNotice?.message?.asString().orEmpty(),
			icon = if (shownNotice?.kind == RecordNoticeKind.StaleEnrollment) {
				Icons.Outlined.Schedule
			} else {
				Icons.Outlined.Info
			}
		)

		TermSummaryView(
			modifier = Modifier
				.fillMaxWidth()
				.testTag(RecordUiTags.SelectedTermSummary),
			item = item
		)

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
