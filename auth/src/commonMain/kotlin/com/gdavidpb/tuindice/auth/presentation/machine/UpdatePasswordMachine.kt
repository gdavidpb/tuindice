package com.gdavidpb.tuindice.auth.presentation.machine

import com.gdavidpb.tuindice.auth.domain.usecase.UpdatePasswordUseCase
import com.gdavidpb.tuindice.auth.presentation.contract.UpdatePassword
import com.gdavidpb.tuindice.auth.presentation.mapper.toUpdatePasswordErrorMessage
import com.gdavidpb.tuindice.auth.presentation.transition.updatePasswordIdleTransitions
import com.gdavidpb.tuindice.auth.presentation.transition.updatePasswordUpdatingTransitions
import com.gdavidpb.tuindice.base.domain.repository.ConfigRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import org.jetbrains.compose.resources.getString
import tuindice.auth.generated.resources.Res
import tuindice.auth.generated.resources.snack_password_updated

class UpdatePasswordMachine(
	private val updatePasswordUseCase: UpdatePasswordUseCase,
	private val configRepository: ConfigRepository
) : ScreenMachine<UpdatePassword.State, UpdatePassword.Effect> {
	override fun initialState(): UpdatePassword.State = UpdatePassword.State.Idle()

	override fun define(
		host: MachineHost<UpdatePassword.Effect>
	): MachineDefinition<UpdatePassword.State> {
		return MachineDefinition.define {
			updatePasswordIdleTransitions(machine = this@UpdatePasswordMachine, host = host)
			updatePasswordUpdatingTransitions(host = host)
		}
	}

	internal fun startUpdate(
		host: MachineHost<UpdatePassword.Effect>,
		state: UpdatePassword.State.Idle
	): UpdatePassword.State.Updating {
		host.launchMachineJob {
			updatePasswordUseCase.execute(state.password).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						UpdatePasswordInternalEvent.PasswordUpdateSucceeded(
							message = getString(Res.string.snack_password_updated)
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						UpdatePasswordInternalEvent.PasswordUpdateFailed(
							message = useCaseState.error.toUpdatePasswordErrorMessage(
								supportEmail = configRepository.getContactEmail()
							)
						)
					)
				}
			}
		}

		return UpdatePassword.State.Updating(
			password = state.password,
			isPasswordVisible = state.isPasswordVisible
		)
	}
}
