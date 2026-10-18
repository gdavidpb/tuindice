package com.gdavidpb.tuindice.summary.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.model.asString
import com.gdavidpb.tuindice.base.presentation.navigation.AssistantLauncher
import com.gdavidpb.tuindice.base.presentation.navigation.LocalAssistantLauncher
import com.gdavidpb.tuindice.base.ui.view.LoadingView
import com.gdavidpb.tuindice.summary.presentation.contract.TermReport
import com.gdavidpb.tuindice.summary.ui.SummaryUiTags
import com.gdavidpb.tuindice.summary.ui.TermReportUiTags
import com.gdavidpb.tuindice.summary.ui.model.TermReportActions
import com.gdavidpb.tuindice.summary.ui.view.TermReportContentView
import com.gdavidpb.tuindice.summary.ui.view.TermReportMessageView

/**
 * The term report: a progress indicator while the report is read, the report itself, or the words that say why it
 * cannot be shown. Its "Preguntar sobre esto" opens the chat about the report, where the assistant is there to be
 * asked.
 */
@Composable
fun TermReportScreen(
	state: TermReport.State,
	actions: TermReportActions,
	modifier: Modifier = Modifier
) {
	val assistantLauncher = LocalAssistantLauncher.current

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(MaterialTheme.colorScheme.background)
	) {
		when (state) {
			TermReport.State.Idle -> Unit

			TermReport.State.Loading -> LoadingView(indicatorTag = SummaryUiTags.LoadingIndicator)

			is TermReport.State.Content -> TermReportContentView(
				report = state.report,
				feedback = state.feedback,
				onAskAssistantClick = askAssistantAction(assistantLauncher, state.assistantContext),
				actions = actions
			)

			is TermReport.State.NoReport -> TermReportMessageView(
				title = state.title.asString(),
				message = state.message.asString(),
				tag = TermReportUiTags.TermReportNoReport
			)

			is TermReport.State.Failed -> TermReportMessageView(
				title = state.title.asString(),
				message = state.message.asString(),
				tag = TermReportUiTags.TermReportFailed,
				onRetryClick = actions.onRetryClick
			)
		}
	}
}

// The chat's launch of the report, or no action at all when the host has no chat to open.
private fun askAssistantAction(
	launcher: AssistantLauncher?,
	context: AssistantScreenContext
): (() -> Unit)? {
	if (launcher == null) return null

	return { launcher.launch(context) }
}
