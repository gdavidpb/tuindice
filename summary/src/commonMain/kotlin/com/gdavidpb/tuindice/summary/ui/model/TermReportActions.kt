package com.gdavidpb.tuindice.summary.ui.model

import androidx.compose.runtime.Stable
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.summary.presentation.model.TermReportActionItem

/**
 * What the term report asks for when it is tapped: a thumb, a button of "Qué sigue", or a retry. [onAskAssistant]
 * asks the assistant about the report, and is null when the assistant cannot be reached from here.
 */
@Stable
class TermReportActions(
	val onRateClick: (useful: Boolean) -> Unit = {},
	val onNextActionClick: (action: TermReportActionItem) -> Unit = {},
	val onRetryClick: () -> Unit = {},
	val onAskAssistant: ((context: AssistantScreenContext) -> Unit)? = null
)
