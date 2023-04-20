package com.gdavidpb.tuindice.login.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.extension.execute
import com.gdavidpb.tuindice.login.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.login.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.login.presentation.processor.UpdatePasswordProcessor

class UpdatePasswordViewModel(
	private val updatePasswordUseCase: UpdatePasswordUseCase
) : BaseViewModel<UpdatePassword.State, UpdatePassword.Action, UpdatePassword.Event>(
	initialViewState = UpdatePassword.State.Idle
) {
	fun signInAction(password: String) =
		emitAction(UpdatePassword.Action.ClickSignIn(password = password))

	fun cancelAction() =
		emitAction(UpdatePassword.Action.CloseDialog)

	override suspend fun handleAction(action: UpdatePassword.Action) {
		when (action) {
			is UpdatePassword.Action.ClickSignIn ->
				execute(
					useCase = updatePasswordUseCase,
					params = action.password,
					processor = UpdatePasswordProcessor { event -> sendEvent(event) })

			is UpdatePassword.Action.CloseDialog -> sendEvent(UpdatePassword.Event.CloseDialog)
		}
	}
}