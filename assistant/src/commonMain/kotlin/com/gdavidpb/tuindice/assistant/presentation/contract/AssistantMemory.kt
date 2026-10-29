package com.gdavidpb.tuindice.assistant.presentation.contract

import com.gdavidpb.tuindice.assistant.presentation.model.MemoryEntryItem
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.assistant.generated.resources.Res
import tuindice.assistant.generated.resources.assistant_memory_title

// What the assistant remembers about the student, reached from Preferencias. The student reads and deletes it.
object AssistantMemory {
	// Every state carries the title of the screen, so the bar says «Memoria del asistente» in all of them.
	sealed class State(
		override val topBarTitle: UiText = UiText.Resource(Res.string.assistant_memory_title),
		override val isTopBarVisible: Boolean = true,
		override val isBottomBarVisible: Boolean = false
	) : ViewState {
		data object Idle : State()
		data object Loading : State()

		// [deleting] holds the ids whose delete is on its way: until it ends, the entry is listed and is not asked again.
		data class Content(val entries: List<MemoryEntryItem>, val deleting: Set<String> = emptySet()) : State()
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
