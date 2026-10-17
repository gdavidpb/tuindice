package com.gdavidpb.tuindice.assistant.presentation.contract

import com.gdavidpb.tuindice.assistant.presentation.model.MemoryEntryItem
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState

// What the assistant remembers about the student, reached from About. The student reads, confirms and deletes it.
object AssistantMemory {
	sealed class State : ViewState {
		data object Idle : State()
		data object Loading : State()
		data class Content(val entries: List<MemoryEntryItem>) : State()
		data object Empty : State()
		data class Failed(val message: String) : State()
	}

	sealed class Action : ViewAction {
		data object Load : Action()
		data object Retry : Action()
		class DeleteEntry(val memoryId: String) : Action()
		class AnswerStillApplies(val memoryId: String, val applies: Boolean) : Action()

		// Asks to clear everything; the confirmation dialog answers with ConfirmClearAll.
		data object ClearAll : Action()
		data object ConfirmClearAll : Action()
	}

	sealed class Effect : ViewEffect {
		class ShowSnackBar(val message: String) : Effect()
		data object ShowClearAllConfirmation : Effect()
	}
}
