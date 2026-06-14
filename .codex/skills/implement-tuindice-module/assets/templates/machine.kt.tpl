package $PACKAGE.presentation.machine

import $PACKAGE.domain.usecase.$OBSERVE_USE_CASE_NAME
import $PACKAGE.domain.usecase.$UPDATE_USE_CASE_NAME
import $PACKAGE.presentation.contract.$FEATURE_NAME
import $PACKAGE.presentation.mapper.toErrorMessage
import $PACKAGE.presentation.transition.$TRANSITIONS_FUNCTION_NAME
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine

class $MACHINE_NAME(
	private val $OBSERVE_USE_CASE_PARAM_NAME: $OBSERVE_USE_CASE_NAME,
	private val $UPDATE_USE_CASE_PARAM_NAME: $UPDATE_USE_CASE_NAME
) : ScreenMachine<$FEATURE_NAME.State, $FEATURE_NAME.Effect> {
	override fun initialState(): $FEATURE_NAME.State = $FEATURE_NAME.State.Loading

	override fun define(host: MachineHost<$FEATURE_NAME.Effect>): MachineDefinition<$FEATURE_NAME.State> {
		return MachineDefinition.define {
			$TRANSITIONS_FUNCTION_NAME(machine = this@$MACHINE_NAME, host = host)
		}
	}

	internal fun startObservation(host: MachineHost<$FEATURE_NAME.Effect>) {
		host.launchMachineJob {
			$OBSERVE_USE_CASE_PARAM_NAME.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						$INTERNAL_EVENT_NAME.$CONTENT_OBSERVED_EVENT_NAME(
							message = useCaseState.value
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						$INTERNAL_EVENT_NAME.$OBSERVATION_FAILED_EVENT_NAME(
							message = useCaseState.error.toErrorMessage()
						)
					)
				}
			}
		}
	}

	internal fun refresh(host: MachineHost<$FEATURE_NAME.Effect>) {
		host.launchMachineJob {
			$UPDATE_USE_CASE_PARAM_NAME.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> host.processInternalEvent(
						$INTERNAL_EVENT_NAME.$REFRESH_STARTED_EVENT_NAME
					)

					is UseCaseState.Data -> Unit

					is UseCaseState.Error -> host.processInternalEvent(
						$INTERNAL_EVENT_NAME.$REFRESH_FAILED_EVENT_NAME(
							message = useCaseState.error.toErrorMessage()
						)
					)
				}
			}
		}
	}
}
