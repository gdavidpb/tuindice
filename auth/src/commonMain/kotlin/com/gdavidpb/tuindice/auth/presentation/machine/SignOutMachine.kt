package com.gdavidpb.tuindice.auth.presentation.machine

import com.gdavidpb.tuindice.auth.domain.usecase.ConfirmSignOutUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.FlushPendingChangesUseCase
import com.gdavidpb.tuindice.auth.domain.usecase.SignOutUseCase
import com.gdavidpb.tuindice.auth.presentation.contract.SignOut
import com.gdavidpb.tuindice.auth.presentation.transition.signOutAnyStateTransitions
import com.gdavidpb.tuindice.auth.presentation.transition.signOutFlushFailedTransitions
import com.gdavidpb.tuindice.auth.presentation.transition.signOutLoggingOutTransitions
import com.gdavidpb.tuindice.auth.presentation.transition.signOutPendingTransitions
import com.gdavidpb.tuindice.auth.presentation.transition.signOutPlainTransitions
import com.gdavidpb.tuindice.base.domain.model.FlushPendingChangesResult
import com.gdavidpb.tuindice.base.domain.model.PendingChanges
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.snack_default_error

class SignOutMachine(
	private val confirmSignOutUseCase: ConfirmSignOutUseCase,
	private val signOutUseCase: SignOutUseCase,
	private val flushPendingChangesUseCase: FlushPendingChangesUseCase
) : ScreenMachine<SignOut.State, SignOut.Effect> {
	override fun initialState(): SignOut.State = SignOut.State.Plain

	override fun define(host: MachineHost<SignOut.Effect>): MachineDefinition<SignOut.State> {
		return MachineDefinition.define {
			signOutPlainTransitions(machine = this@SignOutMachine, host = host)
			signOutPendingTransitions(machine = this@SignOutMachine, host = host)
			signOutFlushFailedTransitions(machine = this@SignOutMachine, host = host)
			signOutLoggingOutTransitions(machine = this@SignOutMachine, host = host)
			signOutAnyStateTransitions(machine = this@SignOutMachine, host = host)
		}
	}

	internal fun initialize(host: MachineHost<SignOut.Effect>, pendingChanges: PendingChanges) {
		if (pendingChanges.totalCount == 0) {
			host.processInternalEvent(SignOutInternalEvent.SignOutInitializedPlain)
		} else {
			host.processInternalEvent(
				SignOutInternalEvent.SignOutInitializedPending(pendingChanges = pendingChanges)
			)
		}
	}

	internal fun confirmSignOut(host: MachineHost<SignOut.Effect>) {
		host.launchMachineJob {
			confirmSignOutUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> host.processInternalEvent(
						SignOutInternalEvent.LoggingOutObserved(
							pendingChanges = null,
							requiresPasswordUpdate = false
						)
					)

					is UseCaseState.Data ->
						if (useCaseState.value.totalCount > 0) {
							host.processInternalEvent(
								SignOutInternalEvent.PendingChangesFound(
									pendingChanges = useCaseState.value
								)
							)
						} else {
							runSignOut(
								host = host,
								onStart = SignOutInternalEvent.LoggingOutObserved(
									pendingChanges = null,
									requiresPasswordUpdate = false
								),
								onError = {
									SignOutInternalEvent.SignOutFailedToPlain(
										message = getString(Res.string.snack_default_error)
									)
								}
							)
						}

					is UseCaseState.Error -> host.processInternalEvent(
						SignOutInternalEvent.SignOutFailedToPlain(
							message = getString(Res.string.snack_default_error)
						)
					)
				}
			}
		}
	}

	internal fun flushAndSignOut(host: MachineHost<SignOut.Effect>, pendingChanges: PendingChanges) {
		host.launchMachineJob {
			flushPendingChangesUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> host.processInternalEvent(
						SignOutInternalEvent.LoggingOutObserved(
							pendingChanges = pendingChanges,
							requiresPasswordUpdate = false
						)
					)

					is UseCaseState.Data -> when (val result = useCaseState.value) {
						FlushPendingChangesResult.Success -> runSignOut(
							host = host,
							onStart = SignOutInternalEvent.LoggingOutObserved(
								pendingChanges = pendingChanges,
								requiresPasswordUpdate = false
							),
							onError = {
								SignOutInternalEvent.FlushFailedObserved(
									pendingChanges = pendingChanges,
									requiresPasswordUpdate = false,
									message = getString(Res.string.snack_default_error)
								)
							}
						)

						is FlushPendingChangesResult.PendingRemaining -> host.processInternalEvent(
							SignOutInternalEvent.FlushFailedObserved(
								pendingChanges = result.pendingChanges,
								requiresPasswordUpdate = false,
								message = null
							)
						)

						is FlushPendingChangesResult.OutdatedCredentials -> host.processInternalEvent(
							SignOutInternalEvent.FlushFailedObserved(
								pendingChanges = result.pendingChanges,
								requiresPasswordUpdate = true,
								message = null
							)
						)
					}

					is UseCaseState.Error -> host.processInternalEvent(
						SignOutInternalEvent.FlushFailedObserved(
							pendingChanges = pendingChanges,
							requiresPasswordUpdate = false,
							message = getString(Res.string.snack_default_error)
						)
					)
				}
			}
		}
	}

	internal fun forceSignOut(
		host: MachineHost<SignOut.Effect>,
		pendingChanges: PendingChanges,
		requiresPasswordUpdate: Boolean
	) {
		host.launchMachineJob {
			runSignOut(
				host = host,
				onStart = SignOutInternalEvent.LoggingOutObserved(
					pendingChanges = pendingChanges,
					requiresPasswordUpdate = requiresPasswordUpdate
				),
				onError = {
					SignOutInternalEvent.FlushFailedObserved(
						pendingChanges = pendingChanges,
						requiresPasswordUpdate = requiresPasswordUpdate,
						message = getString(Res.string.snack_default_error)
					)
				}
			)
		}
	}

	private suspend fun runSignOut(
		host: MachineHost<SignOut.Effect>,
		onStart: SignOutInternalEvent,
		onError: suspend () -> SignOutInternalEvent
	) {
		signOutUseCase.execute(Unit).collect { useCaseState ->
			when (useCaseState) {
				is UseCaseState.Loading -> host.processInternalEvent(onStart)

				is UseCaseState.Data -> host.processInternalEvent(
					SignOutInternalEvent.SignOutSucceeded
				)

				is UseCaseState.Error -> host.processInternalEvent(onError())
			}
		}
	}
}
