package com.gdavidpb.tuindice.login.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.login.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.presentation.reducer.UpdatePasswordReducer

class UpdatePasswordViewModel(
	private val updatePasswordUseCase: UpdatePasswordUseCase,
	private val updatePasswordReducer: UpdatePasswordReducer
) : BaseViewModel<UpdatePassword.State, UpdatePassword.Action, UpdatePassword.Event>(
	initialViewState = UpdatePassword.State.Idle()
) {
	fun signInAction() {
		val currentState = getCurrentState()

		if (currentState is UpdatePassword.State.Idle) {
			val (password) = currentState

			emitAction(UpdatePassword.Action.ClickSignIn(password))
		}
	}

	override suspend fun reducer(action: UpdatePassword.Action) {
		when (action) {
			is UpdatePassword.Action.ClickSignIn ->
				updatePasswordUseCase
					.execute(params = action.password)
					.collect(viewModel = this, reducer = updatePasswordReducer)
		}
	}
}