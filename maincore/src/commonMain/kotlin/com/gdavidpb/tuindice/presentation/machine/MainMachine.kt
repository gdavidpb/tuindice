package com.gdavidpb.tuindice.presentation.machine

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.domain.usecase.ScheduleSyncUseCase
import com.gdavidpb.tuindice.domain.usecase.SetLastMainSectionUseCase
import com.gdavidpb.tuindice.wizard.domain.usecase.ShouldStartWizardUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.error.StartUpUseCaseError
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.mapper.toDestination
import com.gdavidpb.tuindice.presentation.transition.mainAnyStateTransitions
import com.gdavidpb.tuindice.presentation.transition.mainContentTransitions
import kotlinx.coroutines.flow.collect

class MainMachine(
	private val startUpUseCase: StartUpUseCase,
	private val requestReviewUseCase: RequestReviewUseCase,
	private val getUpdateInfoUseCase: GetUpdateInfoUseCase,
	private val scheduleSyncUseCase: ScheduleSyncUseCase,
	private val setLastMainSectionUseCase: SetLastMainSectionUseCase,
	private val shouldStartWizardUseCase: ShouldStartWizardUseCase
) : ScreenMachine<Main.State, Main.Effect> {
	override fun initialState(): Main.State = Main.State.Starting

	override fun define(host: MachineHost<Main.Effect>): MachineDefinition<Main.State> {
		return MachineDefinition.define {
			mainContentTransitions(host = host)
			mainAnyStateTransitions(machine = this@MainMachine, host = host)
		}
	}

	internal fun startUp(host: MachineHost<Main.Effect>) {
		host.launchMachineJob {
			startUpUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> host.processInternalEvent(
						MainInternalEvent.StartUpStarting
					)

					is UseCaseState.Data -> host.processInternalEvent(
						MainInternalEvent.StartUpCompleted(
							startDestination = useCaseState.value.startTarget.toDestination()
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						MainInternalEvent.StartUpFailed(
							noServices = useCaseState.error is StartUpUseCaseError.NoServices
						)
					)
				}
			}
		}
	}

	internal fun requestReview(host: MachineHost<Main.Effect>) {
		host.launchMachineJob {
			requestReviewUseCase.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(MainInternalEvent.ReviewRequested)
				}
			}
		}
	}

	internal fun requestUpdateCheck(host: MachineHost<Main.Effect>) {
		host.launchMachineJob {
			getUpdateInfoUseCase.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						MainInternalEvent.UpdateInfoLoaded(action = useCaseState.value)
					)
				}
			}
		}
	}

	internal fun requestSync(host: MachineHost<Main.Effect>) {
		host.launchMachineJob {
			scheduleSyncUseCase.execute(Unit).collect()
		}
	}

	internal fun setLastMainSection(host: MachineHost<Main.Effect>, section: MainSection) {
		host.launchMachineJob {
			setLastMainSectionUseCase.execute(section).collect()
		}
	}

	internal fun requestWizardStart(host: MachineHost<Main.Effect>) {
		host.launchMachineJob {
			shouldStartWizardUseCase.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data && useCaseState.value) {
					host.processInternalEvent(MainInternalEvent.WizardStartApproved)
				}
			}
		}
	}
}
