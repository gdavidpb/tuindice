package com.gdavidpb.tuindice.summary.ui.view

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.gdavidpb.tuindice.base.presentation.mapper.NewStudentNoRecordTexts
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.ui.style.TuIndiceSpacing
import com.gdavidpb.tuindice.base.ui.view.EmptyStateAnimationView
import com.gdavidpb.tuindice.base.ui.view.IllustratedMessageView
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import org.jetbrains.compose.resources.stringResource
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.summary_failed_retry

// A student the university has no record for yet: not a failure to retry blindly, so the action is
// secondary and waits while a sync runs.
@Composable
fun SummaryNewStudentView(
	isRetryEnabled: Boolean,
	onRetryClick: () -> Unit
) {
	IllustratedMessageView(
		modifier = Modifier
			.testTag(SummaryUiTags.NewStudentContainer)
			.padding(horizontal = TuIndiceSpacing.Dialog)
			.fillMaxSize(),
		title = NewStudentNoRecordTexts.title.asString(),
		message = NewStudentNoRecordTexts.message.asString(),
		actionLabel = stringResource(Res.string.summary_failed_retry),
		onActionClick = onRetryClick,
		titleTestTag = SummaryUiTags.NewStudentTitle,
		messageTestTag = SummaryUiTags.NewStudentMessage,
		actionTestTag = SummaryUiTags.NewStudentRetryButton,
		isActionOutlined = true,
		isActionEnabled = isRetryEnabled,
		headerContent = { EmptyStateAnimationView() }
	)
}
