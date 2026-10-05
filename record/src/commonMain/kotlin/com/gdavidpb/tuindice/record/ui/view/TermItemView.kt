package com.gdavidpb.tuindice.record.ui.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptOutcome
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.NoticeView
import com.gdavidpb.tuindice.base.utils.extension.rememberLastNonNull
import com.gdavidpb.tuindice.record.presentation.model.TermItem
import com.gdavidpb.tuindice.record.ui.RecordUiTags

@Composable
fun TermItemView(
	modifier: Modifier = Modifier,
	item: TermItem,
	onAttemptSelectionChange: (
		attemptId: String,
		newGrade: Int?,
		newOutcome: AttemptOutcome?,
		isSelected: Boolean
	) -> Unit,
	onScrollInProgressChange: (Boolean) -> Unit = {}
) {
	Column(modifier = modifier.fillMaxSize()) {
		// The page's own notice, already placed by the mapper. Always composed, so it animates in and
		// out instead of making the page jump.
		val shownNotice = rememberLastNonNull(item.notice)

		NoticeView(
			modifier = Modifier.padding(vertical = TuIndiceSpacing.Medium),
			visible = item.notice != null,
			title = shownNotice?.title?.asString(),
			message = shownNotice?.message?.asString().orEmpty(),
			// The fallback is never drawn: nothing shows until a notice has brought its own icon.
			icon = shownNotice?.icon ?: Icons.Outlined.Info
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
