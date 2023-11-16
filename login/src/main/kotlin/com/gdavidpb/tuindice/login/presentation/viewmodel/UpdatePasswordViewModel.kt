package com.gdavidpb.tuindice.login.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.login.presentation.action.SetUpdatePasswordActionProcessor
import com.gdavidpb.tuindice.login.presentation.action.UpdatePasswordActionProcessor
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import kotlinx.coroutines.flow.Flow

class UpdatePasswordViewModel(
	private val setUpdatePasswordActionProcessor: SetUpdatePasswordActionProcessor,
	private val updatePasswordActionProcessor: UpdatePasswordActionProcessor
) : BaseViewModel<UpdatePassword.State, UpdatePassword.Action, UpdatePassword.Effect>(
	initialState = UpdatePassword.State.Idle()
) {
	fun signInAction(password: String) =
		sendAction(UpdatePassword.Action.ClickSignIn(password))

	fun setPasswordAction(password: String) =
		sendAction(UpdatePassword.Action.SetPassword(password))

	override fun processAction(
		action: UpdatePassword.Action,
		sideEffect: (UpdatePassword.Effect) -> Unit
	): Flow<Mutation<UpdatePassword.State>> {
		return when (action) {
			is UpdatePassword.Action.SetPassword ->
				setUpdatePasswordActionProcessor.process(action, sideEffect)

			is UpdatePassword.Action.ClickSignIn ->
				updatePasswordActionProcessor.process(action, sideEffect)
		}
	}
}