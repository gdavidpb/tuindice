package com.gdavidpb.tuindice.login.presentation.contract

import androidx.annotation.IdRes
import com.gdavidpb.tuindice.base.domain.model.ServicesStatus

object Splash {
	sealed class State {
		object Starting : State()
		object Started : State()
		object Failed : State()
	}

	sealed class Action {
		class StartUp(val data: String) : Action()
	}

	sealed class Event {
		class NavigateTo(@IdRes val navId: Int) : Event()
		object NavigateToSignIn : Event()
		class ShowNoServicesDialog(val status: ServicesStatus) : Event()
	}
}