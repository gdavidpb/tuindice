package com.gdavidpb.tuindice.login.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.reducer.collect
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.login.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.presentation.reducer.UpdatePasswordReducer

class UpdatePasswordViewModel(
	private val updatePasswordUseCase: UpdatePasswordUseCase,
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
				updatePasswordUseCase
					.execute(params = action.password)
					.collect(viewModel = this, reducer = updatePasswordReducer)

			is UpdatePassword.Action.CloseDialog ->
				sendEvent(UpdatePassword.Event.CloseDialog)
		}
	}
}