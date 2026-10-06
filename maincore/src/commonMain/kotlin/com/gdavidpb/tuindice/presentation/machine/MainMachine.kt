package com.gdavidpb.tuindice.presentation.machine

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.domain.usecase.EnsureMessagingSubscribedUseCase
import com.gdavidpb.tuindice.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.domain.usecase.ObserveOutdatedAppUseCase
import com.gdavidpb.tuindice.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.domain.usecase.ScheduleSyncUseCase
import com.gdavidpb.tuindice.domain.usecase.SetLastMainSectionUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.error.StartUpUseCaseError
import com.gdavidpb.tuindice.domain.usecase.result.StartUpResult
import com.gdavidpb.tuindice.presentation.contract.Main
import com.gdavidpb.tuindice.presentation.mapper.toDestination
import com.gdavidpb.tuindice.presentation.transition.mainAnyStateTransitions
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import org.jetbrains.compose.resources.getString
import tuindice.maincore.generated.resources.Res
import tuindice.maincore.generated.resources.snack_session_reset_on_startup

class MainMachine(
	private val startUpUseCase: StartUpUseCase,
	private val observeOutdatedAppUseCase: ObserveOutdatedAppUseCase,
	private val requestReviewUseCase: RequestReviewUseCase,
	private val getUpdateInfoUseCase: GetUpdateInfoUseCase,
	private val scheduleSyncUseCase: ScheduleSyncUseCase,
	private val ensureMessagingSubscribedUseCase: EnsureMessagingSubscribedUseCase,
	private val setLastMainSectionUseCase: SetLastMainSectionUseCase
) : ScreenMachine<Main.State, Main.Effect> {
	// StartUp is dispatched again when a failed startup is retried, and launchMachineJob
	// accumulates rather than replaces: this handle keeps a single observer of the refusals.
	private var outdatedAppObservationJob: Job? = null

	override fun initialState(): Main.State = Main.State.Starting

	override fun define(host: MachineHost<Main.Effect>): MachineDefinition<Main.State> {
		return MachineDefinition.define {
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
						useCaseState.value.toStartUpEvent()
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

	// The refusals are not replayed, so this starts with the machine (its first input) rather
	// than once the startup has resolved: a request made in between must not go unheard.
	internal fun observeOutdatedApp(host: MachineHost<Main.Effect>) {
		if (outdatedAppObservationJob?.isActive == true) return

		outdatedAppObservationJob = host.launchMachineJob {
			observeOutdatedAppUseCase.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						MainInternalEvent.OutdatedAppObserved(
							outdatedAppState = useCaseState.value
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

	internal fun ensureMessagingSubscribed(host: MachineHost<Main.Effect>) {
		host.launchMachineJob {
			ensureMessagingSubscribedUseCase.execute(Unit).collect()
		}
	}

	internal fun setLastMainSection(host: MachineHost<Main.Effect>, section: MainSection) {
		host.launchMachineJob {
			setLastMainSectionUseCase.execute(section).collect()
		}
	}

	private suspend fun StartUpResult.toStartUpEvent(): MainInternalEvent {
		return when (this) {
			is StartUpResult.Available -> MainInternalEvent.StartUpCompleted(
				startDestination = startTarget.toDestination(),
				sessionResetMessage = if (showSessionResetNotice) {
					getString(Res.string.snack_session_reset_on_startup)
				} else {
					null
				}
			)
			is StartUpResult.AppUnavailable -> MainInternalEvent.AppUnavailableResolved(
				notice = notice
			)
			is StartUpResult.OutdatedApp -> MainInternalEvent.OutdatedAppResolved(
				outdatedAppState = state
			)
		}
	}
}
