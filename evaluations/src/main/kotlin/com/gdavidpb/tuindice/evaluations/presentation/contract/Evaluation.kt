package com.gdavidpb.tuindice.evaluations.presentation.contract

import com.gdavidpb.tuindice.base.utils.extension.ViewAction
import com.gdavidpb.tuindice.base.utils.extension.ViewEvent
import com.gdavidpb.tuindice.base.utils.extension.ViewState

object Evaluation {
	sealed class State : ViewState {
		object Loading : Evaluation.State()
	}

	sealed class Action : ViewAction {
	}

	sealed class Event : ViewEvent {
	}
}