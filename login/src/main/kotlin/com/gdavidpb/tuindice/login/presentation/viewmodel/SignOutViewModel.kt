package com.gdavidpb.tuindice.login.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.base.utils.extension.collect
import com.gdavidpb.tuindice.login.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.login.presentation.contract.SignOut
import com.gdavidpb.tuindice.login.presentation.reducer.SignOutReducer

class SignOutViewModel(
	private val signOutUseCase: SignOutUseCase,
	private val signOutReducer: SignOutReducer
) : BaseViewModel<SignOut.State, SignOut.Action, SignOut.Event>(initialViewState = SignOut.State.Idle) {

	fun signOutAction() =
		emitAction(SignOut.Action.ConfirmSignOut)

	override suspend fun reducer(action: SignOut.Action) {
		when (action) {
			is SignOut.Action.ConfirmSignOut ->
				signOutUseCase
					.execute(Unit)
					.collect(viewModel = this, reducer = signOutReducer)
		}
	}
}