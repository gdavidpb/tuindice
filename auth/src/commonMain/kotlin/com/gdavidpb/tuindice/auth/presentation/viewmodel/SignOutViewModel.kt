package com.gdavidpb.tuindice.auth.presentation.viewmodel

import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.presentation.machine.SignOutMachine
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel

class SignOutViewModel(
	override val screenMachine: SignOutMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<SignOut.State, SignOut.Action, SignOut.Effect>(
	name = "sign_out",
	initialState = screenMachine.initialState(),
	dispatchers = dispatchers
) {
	fun initializeAction(pendingChanges: PendingChanges) =
		sendAction(SignOut.Action.Initialize(pendingChanges))

	fun signOutAction(resolvedPendingChanges: PendingChanges? = null) =
		sendAction(SignOut.Action.ClickSignOut(resolvedPendingChanges))

	fun retryFlushAndSignOutAction(pendingChanges: PendingChanges) =
		sendAction(SignOut.Action.RetryFlushAndSignOut(pendingChanges))

	fun forceSignOutAction() =
		sendAction(SignOut.Action.ForceSignOut)
}
