package com.gdavidpb.tuindice.summary.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.summary.presentation.mapper.summaryStatisticsAssistantContext
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubTarget
import com.gdavidpb.tuindice.summary.presentation.model.SummaryStatisticsItem
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.top_bar_summary_statistics

/**
 * The Statistics screen, pushed from the summary hub: read-only, with its top bar and no bottom bar. It reads the
 * statistics of the hub and says nothing of its own.
 */
object SummaryStatistics {
	sealed class State(
		override val topBarTitle: UiText = UiText.Resource(Res.string.top_bar_summary_statistics),
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = false
	) : ViewState {
		// Only a loaded content knows what the statistics show; every other state describes the screen alone.
		override val assistantContext: AssistantScreenContext
			get() = AssistantScreenContext(screen = AssistantScreen.STATISTICS)

		data object Idle : State()

		data object Loading : State()

		data class Content(
			val statistics: SummaryStatisticsItem
		) : State() {
			// The closed terms of the statistics can be shared as an image, from the top bar.
			override val topBarConfig: TopBarConfig = TopBarConfig.Statistics

			override val assistantContext: AssistantScreenContext
				get() = summaryStatisticsAssistantContext(this)
		}

		// The record is not ready to be read, so there are no statistics: the screen says so and does not retry.
		data class Unavailable(
			val title: UiText,
			val message: UiText
		) : State()
	}

	sealed class Action : ViewAction {
		data object ObserveStatistics : Action()

		// The screen is back in front: the clock is read again instead of waiting for its next tick.
		data object Resumed : Action()

		// A link of the screen was tapped: it asks to be taken to the screen that owns what it says.
		class OpenTarget(val target: SummaryHubTarget) : Action()
	}

	sealed class Effect : ViewEffect {
		// Take the student to the screen that owns what the tapped link says, which is [target].
		class NavigateTo(val target: SummaryHubTarget) : Effect()
	}
}
