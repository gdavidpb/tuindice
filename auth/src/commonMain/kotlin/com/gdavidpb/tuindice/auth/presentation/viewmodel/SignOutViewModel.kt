package com.gdavidpb.tuindice.auth.presentation.viewmodel

import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.presentation.Mutation
import com.gdavidpb.tuindice.base.presentation.viewmodel.BaseViewModel
import com.gdavidpb.tuindice.auth.presentation.action.ConfirmSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.FlushAndSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.ForceSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.InitializeSignOutActionProcessor
import com.gdavidpb.tuindice.auth.presentation.action.OpenUpdatePasswordActionProcessor
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import kotlinx.coroutines.flow.Flow

class SignOutViewModel(
	private val initializeSignOutActionProcessor: InitializeSignOutActionProcessor,
	private val confirmSignOutActionProcessor: ConfirmSignOutActionProcessor,
	private val flushAndSignOutActionProcessor: FlushAndSignOutActionProcessor,
	private val forceSignOutActionProcessor: ForceSignOutActionProcessor,
	private val openUpdatePasswordActionProcessor: OpenUpdatePasswordActionProcessor
) : BaseViewModel<SignOut.State, SignOut.Action, SignOut.Effect>(
	initialState = SignOut.State.Plain
) {

	fun initializeAction(pendingChanges: PendingChanges) =
		sendAction(SignOut.Action.Initialize(pendingChanges))

	fun signOutAction(resolvedPendingChanges: PendingChanges? = null) {
		val currentState = state.value.withResolvedPendingChanges(resolvedPendingChanges)
		sendAction(
			when (currentState) {
				SignOut.State.Plain,
				is SignOut.State.LoggingOut,
				-> SignOut.Action.ConfirmSignOut

				is SignOut.State.Pending ->
					SignOut.Action.FlushAndSignOut(currentState.pendingChanges)

				is SignOut.State.FlushFailed ->
					if (currentState.requiresPasswordUpdate) {
						SignOut.Action.OpenUpdatePassword
					} else {
						SignOut.Action.FlushAndSignOut(currentState.pendingChanges)
					}
			}
		)
	}

	fun forceSignOutAction() {
		val currentState = state.value as? SignOut.State.FlushFailed ?: return
		sendAction(
			SignOut.Action.ForceSignOut(
				pendingChanges = currentState.pendingChanges,
				requiresPasswordUpdate = currentState.requiresPasswordUpdate
			)
		)
	}

	override suspend fun processAction(
		action: SignOut.Action,
		sideEffect: (SignOut.Effect) -> Unit
	): Flow<Mutation<SignOut.State>> {
		return when (action) {
			is SignOut.Action.Initialize ->
				initializeSignOutActionProcessor.process(action, sideEffect)

			SignOut.Action.ConfirmSignOut ->
				confirmSignOutActionProcessor.process(SignOut.Action.ConfirmSignOut, sideEffect)

			is SignOut.Action.FlushAndSignOut ->
				flushAndSignOutActionProcessor.process(action, sideEffect)

			is SignOut.Action.ForceSignOut ->
				forceSignOutActionProcessor.process(action, sideEffect)

			SignOut.Action.OpenUpdatePassword ->
				openUpdatePasswordActionProcessor.process(SignOut.Action.OpenUpdatePassword, sideEffect)
		}
	}

	private fun SignOut.State.withResolvedPendingChanges(
		pendingChanges: PendingChanges?
	): SignOut.State {
		return if (
			this == SignOut.State.Plain &&
			pendingChanges != null &&
			pendingChanges.totalCount > 0
		) {
			SignOut.State.Pending(pendingChanges)
		} else {
			this
		}
	}
}
