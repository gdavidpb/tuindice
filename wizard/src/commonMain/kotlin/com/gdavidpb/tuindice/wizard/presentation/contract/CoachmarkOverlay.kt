package com.gdavidpb.tuindice.wizard.presentation.contract

import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState
import com.gdavidpb.tuindice.wizard.presentation.model.Coachmark
import com.gdavidpb.tuindice.wizard.presentation.model.CoachmarkSurface

object CoachmarkOverlay {
	data class State(
		val activeCoachmark: Coachmark? = null,
		val previousCoachmarks: List<Coachmark> = emptyList(),
		val pendingCoachmarks: List<Coachmark> = emptyList(),
		val currentVisitKey: String? = null,
		val presentedVisitKeys: Set<String> = emptySet()
	) : ViewState {
		val isVisible: Boolean
			get() = activeCoachmark != null

		val hasNextCoachmark: Boolean
			get() = pendingCoachmarks.size > 1

		val hasPreviousCoachmark: Boolean
			get() = previousCoachmarks.isNotEmpty()
	}

	sealed class Action : ViewAction {
		class SurfaceChanged(val surface: CoachmarkSurface) : Action()
		data object PreviousActionClick : Action()
		data object PrimaryActionClick : Action()
	}

	sealed class Effect : ViewEffect
}
