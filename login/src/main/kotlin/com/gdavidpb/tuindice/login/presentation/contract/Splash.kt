package com.gdavidpb.tuindice.login.presentation.contract

import androidx.annotation.IdRes
import com.gdavidpb.tuindice.base.domain.model.ServicesStatus
import com.gdavidpb.tuindice.base.presentation.reducer.ViewAction
import com.gdavidpb.tuindice.base.presentation.reducer.ViewEvent
import com.gdavidpb.tuindice.base.presentation.reducer.ViewState

object Splash {
	sealed class State : ViewState {
		object Starting : State()
		object Started : State()
		object Failed : State()
	}

	sealed class Action : ViewAction {
		class StartUp(val data: String) : Action()
	}

	sealed class Event : ViewEvent {
		class NavigateTo(@IdRes val navId: Int) : Event()
		object NavigateToSignIn : Event()
		class ShowNoServicesDialog(val status: ServicesStatus) : Event()
	}
}