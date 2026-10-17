package com.gdavidpb.tuindice.presentation.model

import com.gdavidpb.tuindice.base.presentation.model.TopBarAction

// One place of the top bar, in the order it is drawn: the assistant, Record's view switch, or an action of the screen.
sealed interface TopBarSlot {
	data object Assistant : TopBarSlot

	data object ViewModeSelector : TopBarSlot

	data class Action(
		val action: TopBarAction
	) : TopBarSlot
}
