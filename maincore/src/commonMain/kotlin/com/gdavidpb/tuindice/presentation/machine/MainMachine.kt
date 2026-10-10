package com.gdavidpb.tuindice.presentation.machine

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.domain.usecase.EnsureMessagingSubscribedUseCase
import com.gdavidpb.tuindice.domain.usecase.GetPendingChangesUseCase
import com.gdavidpb.tuindice.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.domain.usecase.ObserveOutdatedAppUseCase
import com.gdavidpb.tuindice.domain.usecase.ObserveSessionInvalidationUseCase
import com.gdavidpb.tuindice.domain.usecase.ObserveSyncStatusUseCase
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
import tuindice.maincore.generated.resources.snack_pending_changes_unavailable
import tuindice.maincore.generated.resources.snack_session_invalidated
import tuindice.maincore.generated.resources.snack_session_reset_on_startup

class MainMachine(
	private val startUpUseCase: StartUpUseCase,
	private val observeOutdatedAppUseCase: ObserveOutdatedAppUseCase,
	private val requestReviewUseCase: RequestReviewUseCase,
	private val getUpdateInfoUseCase: GetUpdateInfoUseCase,
	private val scheduleSyncUseCase: ScheduleSyncUseCase,
	private val ensureMessagingSubscribedUseCase: EnsureMessagingSubscribedUseCase,
	private val setLastMainSectionUseCase: SetLastMainSectionUseCase,
	private val observeSyncStatusUseCase: ObserveSyncStatusUseCase,
	private val observeSessionInvalidationUseCase: ObserveSessionInvalidationUseCase,
	private val getPendingChangesUseCase: GetPendingChangesUseCase
) : ScreenMachine<Main.State, Main.Effect> {
	// StartUp is dispatched again when a failed startup is retried, and launchMachineJob
	// accumulates rather than replaces: this handle keeps a single observer of the refusals.
	private var outdatedAppObservationJob: Job? = null
	private var syncStatusObservationJob: Job? = null
	private var sessionInvalidationObservationJob: Job? = null

	// Reading the pending work takes a moment: a second tap on "sign out" while it runs must not
	// open the dialog twice.
	private var signOutPreparationJob: Job? = null

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

	// What is followed while the content is up, started by the startup that resolved to it. A
	// retried startup lands here again: the sync status is observed afresh, so the new content
	// starts from the current one instead of the default, and the session keeps its one observer.
	internal fun observeContent(host: MachineHost<Main.Effect>) {
		syncStatusObservationJob?.cancel()
		syncStatusObservationJob = host.launchMachineJob {
			observeSyncStatusUseCase.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						MainInternalEvent.SyncStatusObserved(syncStatus = useCaseState.value)
					)
				}
			}
		}

		if (sessionInvalidationObservationJob?.isActive == true) return

		// An invalidation that arrives before this starts is kept by the repository and delivered
		// here, once: there is no content to take to the sign-in before the startup resolves.
		sessionInvalidationObservationJob = host.launchMachineJob {
			observeSessionInvalidationUseCase.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						MainInternalEvent.SessionInvalidationObserved(
							message = getString(Res.string.snack_session_invalidated)
						)
					)
				}
			}
		}
	}

	// The preparatory step of signing out: what the device still has to send decides what the
	// dialog warns about. Signing out itself belongs to the dialog.
	internal fun prepareSignOut(host: MachineHost<Main.Effect>) {
		if (signOutPreparationJob?.isActive == true) return

		signOutPreparationJob = host.launchMachineJob {
			getPendingChangesUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						MainInternalEvent.SignOutPrepared(pendingChanges = useCaseState.value)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						MainInternalEvent.SignOutPreparationFailed(
							message = getString(Res.string.snack_pending_changes_unavailable)
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
