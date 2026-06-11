package com.gdavidpb.tuindice.auth.presentation.contract

import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.presentation.ViewAction
import com.gdavidpb.tuindice.base.presentation.ViewEffect
import com.gdavidpb.tuindice.base.presentation.ViewState

object SignOut {
	sealed class State : ViewState {
		data object Plain : State()

		data class Pending(
			val pendingChanges: PendingChanges
		) : State()

		data class FlushFailed(
			val pendingChanges: PendingChanges,
			val requiresPasswordUpdate: Boolean
		) : State()

		data class LoggingOut(
			val pendingChanges: PendingChanges? = null,
			val requiresPasswordUpdate: Boolean = false
		) : State()
	}

	sealed class Action : ViewAction {
		data class Initialize(
			val pendingChanges: PendingChanges
		) : Action()
		data object ConfirmSignOut : Action()
		data class FlushAndSignOut(
			val pendingChanges: PendingChanges
		) : Action()
		data class ForceSignOut(
			val pendingChanges: PendingChanges,
			val requiresPasswordUpdate: Boolean
		) : Action()
		data object OpenUpdatePassword : Action()
	}

	sealed class Effect : ViewEffect {
		data object NavigateToSignIn : Effect()
		data object NavigateToUpdatePassword : Effect()

		class ShowSnackBar(
			val message: String
		) : Effect()
	}
}
