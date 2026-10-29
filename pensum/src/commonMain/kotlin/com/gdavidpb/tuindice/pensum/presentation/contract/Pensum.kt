package com.gdavidpb.tuindice.pensum.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityKind
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantEntityRef
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreen
import com.gdavidpb.tuindice.base.domain.model.assistant.AssistantScreenContext
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.TopBarConfig
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.pensum.presentation.model.PensumScreenModel
import tuindice.pensum.generated.resources.Res
import tuindice.pensum.generated.resources.pensum_failed_message
import tuindice.pensum.generated.resources.top_bar_pensum

object Pensum {
	sealed class State(
		override val topBarTitle: UiText = UiText.Resource(Res.string.top_bar_pensum),
		override val topBarConfig: TopBarConfig = TopBarConfig.Pensum,
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = true
	) : ViewState {
		override val assistantContext: AssistantScreenContext
			get() = AssistantScreenContext(screen = AssistantScreen.PENSUM)

		data object Idle : State()

		data object Loading : State()

		data object Empty : State()

		data object RecordDataUnavailable : State()

		data class Content(
			val model: PensumScreenModel,
			val isRefreshing: Boolean = false,
			val localDataMessage: UiText? = null,
			val isSummaryCollapsed: Boolean = false
		) : State() {
			override val assistantContext: AssistantScreenContext
				get() = AssistantScreenContext(
					screen = AssistantScreen.PENSUM,
					entities = listOf(
						AssistantEntityRef(
							kind = AssistantEntityKind.PENSUM,
							id = model.pensumId
						)
					),
					filters = emptyMap(),
					visibleFields = mapOf(
						"approvedCredits" to model.approvedCredits.toString(),
						"totalCredits" to model.totalCredits.toString()
					)
				)
		}

		data class Failed(
			val message: UiText = UiText.Resource(Res.string.pensum_failed_message)
		) : State()
	}

	sealed class Action : ViewAction {
		data object ObservePensum : Action()
		data object EnsurePensumLoaded : Action()
		data object RefreshPensum : Action()
		class SelectSelection(
			val year: Int,
			val modalityId: String
		) : Action()
		data object ToggleSummaryCollapsed : Action()
	}

	sealed class Effect : ViewEffect
}
