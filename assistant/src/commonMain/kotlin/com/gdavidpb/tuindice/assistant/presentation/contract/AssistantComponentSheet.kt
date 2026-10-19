package com.gdavidpb.tuindice.assistant.presentation.contract

import com.gdavidpb.tuindice.assistant.presentation.model.ChatComponentItem
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.chatcontract.component.NavTarget
import com.gdavidpb.tuindice.chatcontract.context.EntityRef

// One component of the conversation, opened on its own sheet. The sheet reads the component from the conversation store
// and follows it while it is open.
object AssistantComponentSheet {
	sealed class State : ViewState {
		data object Idle : State()

		data class Content(val messageId: String, val item: ChatComponentItem) : State()

		// The component left the conversation: the sheet closes with Effect.Dismiss.
		data object Missing : State()
	}

	sealed class Action : ViewAction {
		// The sheet opened on the component [componentId] of the message [messageId].
		class Open(val messageId: String, val componentId: String) : Action()

		// A link inside the component was tapped: the sheet goes to its target and closes.
		class OpenNavTarget(val target: NavTarget, val entityRefs: List<EntityRef>) : Action()

		// An action the component emitted that is not a link. The sheet does not take it: the message keeps it.
		class Unhandled(val action: AssistantChat.Action) : Action()
	}

	sealed class Effect : ViewEffect {
		data object Dismiss : Effect()

		class NavigateTo(val target: NavTarget, val entityRefs: List<EntityRef>) : Effect()
	}
}
