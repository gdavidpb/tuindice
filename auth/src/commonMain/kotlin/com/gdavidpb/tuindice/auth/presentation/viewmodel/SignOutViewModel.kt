package com.gdavidpb.tuindice.auth.presentation.viewmodel

import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.auth.presentation.action.SignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import kotlinx.coroutines.flow.Flow

class SignOutViewModel(
	private val signOutActionProcessor: SignOutActionProcessor
) : BaseViewModel<SignOut.State, SignOut.Action, SignOut.Effect>(initialState = SignOut.State.Idle) {

	fun signOutAction() =
		sendAction(SignOut.Action.ConfirmSignOut)

	override suspend fun processAction(
		action: SignOut.Action,
		sideEffect: (SignOut.Effect) -> Unit
	): Flow<Mutation<SignOut.State>> {
		return when (action) {
			is SignOut.Action.ConfirmSignOut ->
				signOutActionProcessor.process(action, sideEffect)
		}
	}
}