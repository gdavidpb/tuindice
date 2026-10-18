package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.mapper.commonUnexpectedErrorMessage
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.summary.domain.usecase.GetCameraAvailabilityUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.SummaryObservations
import com.gdavidpb.tuindice.summary.domain.usecase.SummaryTermReports
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.mapper.toRefreshMessage
import com.gdavidpb.tuindice.summary.presentation.mapper.toRemoveMessage
import com.gdavidpb.tuindice.summary.presentation.mapper.toShortName
import com.gdavidpb.tuindice.summary.presentation.mapper.toSummaryHubItem
import com.gdavidpb.tuindice.summary.presentation.mapper.toSummarySyncItem
import com.gdavidpb.tuindice.summary.presentation.mapper.toSummaryTermReportItem
import com.gdavidpb.tuindice.summary.presentation.mapper.toUploadMessage
import com.gdavidpb.tuindice.summary.presentation.transition.anyStateTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.contentTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.failedTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.idleTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.loadingTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.summaryTermReportTransitions
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import org.jetbrains.compose.resources.getString
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.snack_profile_picture_removed
import tuindice.summary.generated.resources.snack_profile_picture_updated

class SummaryMachine(
	private val observations: SummaryObservations,
	private val getCameraAvailabilityUseCase: GetCameraAvailabilityUseCase,
	private val updateUserUseCase: UpdateUserUseCase,
	private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
	private val removeProfilePictureUseCase: RemoveProfilePictureUseCase,
	private val termReports: SummaryTermReports
) : ScreenMachine<Summary.State, Summary.Effect> {
	// The hub's observation is restarted when the screen comes back, which the user's and the sync's
	// do not need: the job is kept to cancel it.
	private var hubObservationJob: Job? = null

	override fun initialState(): Summary.State = Summary.State.Idle()

	override fun define(host: MachineHost<Summary.Effect>): MachineDefinition<Summary.State> {
		return MachineDefinition.define {
			idleTransitions(machine = this@SummaryMachine, host = host)
			loadingTransitions(machine = this@SummaryMachine, host = host)
			failedTransitions(machine = this@SummaryMachine, host = host)
			contentTransitions(machine = this@SummaryMachine, host = host)
			anyStateTransitions(machine = this@SummaryMachine, host = host)
			summaryTermReportTransitions()
		}
	}

	internal fun startObservation(host: MachineHost<Summary.Effect>) {
		host.launchMachineJob {
			observations.user.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> {
						val user = useCaseState.value

						host.processInternalEvent(
							SummaryInternalEvent.UserObserved(
								content = with(user) {
									Summary.State.Content(
										name = toShortName(),
										careerName = careerName,
										grade = grade.toFloat(),
										profilePictureUrl = pictureUrl,
										profilePictureVersion = pictureVersion,
										isProfilePictureLoading = false,
										isUserRefreshing = false
									)
								}
							)
						)
					}

					is UseCaseState.Error -> host.processInternalEvent(
						SummaryInternalEvent.ObservationFailed(
							message = commonUnexpectedErrorMessage()
						)
					)
				}
			}
		}

		// The hub has its own too: it needs neither the user nor the sync, and it has the clock.
		startHubObservation(host = host)

		// The line of an unread term report is read apart from the hub. The service is asked for its reports as the summary
		// opens, and the line follows the reports it has.
		launchTermReportRefresh(host = host, termReports = termReports)

		host.launchMachineJob {
			termReports.entry.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						SummaryInternalEvent.TermReportEntryObserved(
							entry = useCaseState.value?.toSummaryTermReportItem()
						)
					)
				}
			}
		}

		// Its own observation: the user's never emits for an account with nothing stored, and the
		// failed screen still has to learn why from the sync.
		host.launchMachineJob {
			observations.sync.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						SummaryInternalEvent.SyncObserved(
							sync = useCaseState.value.toSummarySyncItem()
						)
					)
				}
			}
		}
	}

	// A hub that failed to read keeps the one it has: its sources report their own failures and
	// are read as empty, so what reaches here is only a failure to put them together.
	private fun startHubObservation(host: MachineHost<Summary.Effect>) {
		if (hubObservationJob?.isActive == true) return

		hubObservationJob = host.launchMachineJob {
			observations.hub.execute(Unit).collect { useCaseState ->
				if (useCaseState is UseCaseState.Data) {
					host.processInternalEvent(
						SummaryInternalEvent.HubObserved(hub = useCaseState.value.toSummaryHubItem())
					)
				}
			}
		}
	}

	// The first resumption is the screen's own opening, not a return: ResumedEffect fires for the
	// RESUMED the screen is born in, and the first observation reads the clock anyway. It restarts
	// nothing, whichever of the two arrives first. Only the ones after it are a return.
	private var hasSeenTheOpening = false

	internal fun resumed(host: MachineHost<Summary.Effect>) {
		if (!hasSeenTheOpening) {
			hasSeenTheOpening = true
			return
		}

		// A return before any observation does nothing: the observation reads everything when it starts.
		if (hubObservationJob == null) return

		restartHubObservation(host = host)

		// A return asks the service for the reports again, as the opening does: a report written while the app was in
		// the background reaches the line without the student closing the summary.
		launchTermReportRefresh(host = host, termReports = termReports)
	}

	// Coming back from the background: the clock's ticker sleeps with the process, and what the hub
	// shows can be up to a minute behind until it wakes. A new observation reads the clock first
	// thing, so the hub is laid out again for the moment it is now. Nothing to do before the first
	// observation: it reads the clock as well.
	private fun restartHubObservation(host: MachineHost<Summary.Effect>) {
		val running = hubObservationJob ?: return

		running.cancel()
		hubObservationJob = null
		startHubObservation(host = host)
	}

	internal fun resolveCameraAvailability(host: MachineHost<Summary.Effect>) {
		host.launchMachineJob {
			getCameraAvailabilityUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						SummaryInternalEvent.CameraAvailabilityResolved(
							isCameraAvailable = useCaseState.value
						)
					)

					// A device that cannot say still gets its options: picking does not
					// need a camera, so only taking a picture is left out.
					is UseCaseState.Error -> host.processInternalEvent(
						SummaryInternalEvent.CameraAvailabilityResolved(
							isCameraAvailable = false
						)
					)
				}
			}
		}
	}

	internal fun refreshUser(host: MachineHost<Summary.Effect>) {
		host.launchMachineJob {
			updateUserUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> host.processInternalEvent(
						SummaryInternalEvent.RefreshSucceeded
					)

					is UseCaseState.Error -> host.processInternalEvent(
						SummaryInternalEvent.RefreshFailed(
							message = useCaseState.error.toRefreshMessage()
						)
					)
				}
			}
		}
	}

	internal fun uploadProfilePicture(host: MachineHost<Summary.Effect>, file: PlatformFile) {
		host.launchMachineJob {
			uploadProfilePictureUseCase.execute(params = file).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> host.processInternalEvent(
						SummaryInternalEvent.ProfilePictureUploadStarted(
							previewPath = file.path
						)
					)

					is UseCaseState.Data -> host.processInternalEvent(
						SummaryInternalEvent.ProfilePictureUploadSucceeded(
							message = getString(Res.string.snack_profile_picture_updated)
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						SummaryInternalEvent.ProfilePictureUploadFailed(
							message = useCaseState.error.toUploadMessage()
						)
					)
				}
			}
		}
	}

	internal fun removeProfilePicture(host: MachineHost<Summary.Effect>) {
		host.launchMachineJob {
			removeProfilePictureUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> host.processInternalEvent(
						SummaryInternalEvent.ProfilePictureRemovalStarted
					)

					is UseCaseState.Data -> host.processInternalEvent(
						SummaryInternalEvent.ProfilePictureRemovalSucceeded(
							message = getString(Res.string.snack_profile_picture_removed)
						)
					)

					is UseCaseState.Error -> host.processInternalEvent(
						SummaryInternalEvent.ProfilePictureRemovalFailed(
							message = useCaseState.error.toRemoveMessage(),
							clearProfilePicture = useCaseState.error == ProfilePictureUseCaseError.NotFound
						)
					)
				}
			}
		}
	}
}

// The service is asked for the term reports. Its answer reaches the line through the entry observation.
private fun launchTermReportRefresh(host: MachineHost<Summary.Effect>, termReports: SummaryTermReports) {
	host.launchMachineJob {
		termReports.refresh.execute(Unit).collect()
	}
}
