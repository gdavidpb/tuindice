package com.gdavidpb.tuindice.login.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.presentation.processor.UpdatePasswordReducer

class UpdatePasswordViewModel(
	private val updatePasswordReducer: UpdatePasswordReducer
) : BaseViewModel<UpdatePassword.State, UpdatePassword.Action, UpdatePassword.Event>(
	initialViewState = UpdatePassword.State.Idle
) {
	fun signInAction(password: String) =
		emitAction(UpdatePassword.Action.ClickSignIn(password = password))

	fun cancelAction() =
		emitAction(UpdatePassword.Action.CloseDialog)

	override suspend fun reducer(action: UpdatePassword.Action) {
		when (action) {
			is UpdatePassword.Action.ClickSignIn ->
				updatePasswordReducer.reduce(
					action = action,
					stateProducer = ::setState,
					eventProducer = ::sendEvent
				)

			is UpdatePassword.Action.CloseDialog ->
				sendEvent(UpdatePassword.Event.CloseDialog)
		}
	}
}