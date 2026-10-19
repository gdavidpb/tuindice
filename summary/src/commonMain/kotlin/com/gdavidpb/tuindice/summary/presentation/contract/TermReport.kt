package com.gdavidpb.tuindice.summary.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantNavTarget
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.model.TermReportActionItem
import com.gdavidpb.tuindice.summary.presentation.model.TermReportItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.top_bar_term_report

/**
 * The term report ("Tu trimestre"): what the assistant wrote about one term, beside the figures of that term read from
 * the record. It is pushed over the summary, with a back arrow, and its share action sends the term to the sheet that
 * shares terms. "Preguntar sobre esto" is the screen's own, as the other screens' are: it opens the chat with the
 * report as context. The model writes only the sections and the words of the actions; every figure is the record's.
 */
object TermReport {
	sealed class State(
		override val topBarTitle: UiText = UiText.Resource(Res.string.top_bar_term_report),
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = false
	) : ViewState {
		// Only a loaded report knows what it says; every other state describes the screen alone.
		override val assistantContext: AssistantScreenContext
			get() = AssistantScreenContext(screen = AssistantScreen.TERM_REPORT)

		data object Idle : State()

		data object Loading : State()

		// The thumb the student chose is kept while the report is on screen: it is the one the screen shows as selected.
		// [feedback] is the thumb the screen shows as chosen: the student's vote, or the one confirmed when there is none
		// in flight. The share action is in the bar only when the report's term can be shared.
		data class Content(
			val report: TermReportItem,
			val feedback: Boolean? = null
		) : State() {
			override val topBarConfig: TopBarConfig?
				get() = if (report.isShareable) TopBarConfig.TermReport else null

			override val assistantContext: AssistantScreenContext
				get() = report.assistantContext
		}

		// The service has no report of the term, or the record holds no term of it: the screen says so and does not retry.
		data class NoReport(
			val title: UiText,
			val message: UiText
		) : State()

		// The report could not be read at the moment: the screen offers to try again.
		data class Failed(
			val title: UiText,
			val message: UiText
		) : State()
	}

	sealed class Action : ViewAction {
		// The screen opens on the report of [insightId]: it is read, and it is marked as seen once it is on screen.
		class Open(val insightId: String) : Action()

		data object Retry : Action()

		// The student's thumbs on the report: [useful] is the one tapped.
		class Rate(val useful: Boolean) : Action()

		data object Share : Action()

		// A button of "Qué sigue" was tapped: it asks to be taken to the screen it opens.
		class OpenNextAction(val action: TermReportActionItem) : Action()
	}

	sealed class Effect : ViewEffect {
		// The sheet that shares the term of [termKey], pushed over the report.
		class NavigateToShareTerm(val termKey: String) : Effect()

		// The screen that [target] opens, with the records it needs, as the chat opens it.
		class NavigateToAssistantTarget(
			val target: AssistantNavTarget,
			val entityRefs: List<AssistantEntityRef>
		) : Effect()
	}
}
