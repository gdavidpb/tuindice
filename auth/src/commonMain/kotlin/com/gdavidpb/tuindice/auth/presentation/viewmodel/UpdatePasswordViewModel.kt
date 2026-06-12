package com.gdavidpb.tuindice.auth.presentation.viewmodel

import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.auth.presentation.machine.UpdatePasswordMachine
import com.gdavidpb.tuindice.base.domain.dispatcher.DefaultTuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.presentation.statemachine.StateMachineViewModel

class UpdatePasswordViewModel(
	override val screenMachine: UpdatePasswordMachine,
	override val eventPublisher: EventPublisher,
	dispatchers: TuIndiceDispatchers = DefaultTuIndiceDispatchers
) : StateMachineViewModel<UpdatePassword.State, UpdatePassword.Action, UpdatePassword.Effect>(
	name = "update_password",
	initialState = screenMachine.initialState(),
	dispatchers = dispatchers
) {
	fun signInAction() =
		sendAction(UpdatePassword.Action.ClickSignIn)

	fun setPasswordAction(password: String) =
		sendAction(UpdatePassword.Action.SetPassword(password))

	fun togglePasswordVisibilityAction() =
		sendAction(UpdatePassword.Action.TogglePasswordVisibility)
}
