package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineDefinition
import com.gdavidpb.tuindice.base.presentation.statemachine.MachineHost
import com.gdavidpb.tuindice.base.presentation.statemachine.ScreenMachine
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.error.ProfilePictureUseCaseError
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.mapper.formatLastUpdate
import com.gdavidpb.tuindice.summary.presentation.mapper.toRefreshMessage
import com.gdavidpb.tuindice.summary.presentation.mapper.toRemoveMessage
import com.gdavidpb.tuindice.summary.presentation.mapper.toUploadMessage
import com.gdavidpb.tuindice.summary.presentation.mapper.toShortName
import com.gdavidpb.tuindice.summary.presentation.transition.anyStateTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.contentTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.failedTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.idleTransitions
import com.gdavidpb.tuindice.summary.presentation.transition.loadingTransitions
import io.github.vinceglb.filekit.PlatformFile
import org.jetbrains.compose.resources.getString
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.snack_default_error
import tuindice.summary.generated.resources.snack_profile_picture_removed
import tuindice.summary.generated.resources.snack_profile_picture_updated
import tuindice.summary.generated.resources.text_sync_healthy

class SummaryMachine(
	private val observeUserUseCase: ObserveUserUseCase,
	private val updateUserUseCase: UpdateUserUseCase,
	private val uploadProfilePictureUseCase: UploadProfilePictureUseCase,
	private val removeProfilePictureUseCase: RemoveProfilePictureUseCase
) : ScreenMachine<Summary.State, Summary.Effect> {
	override fun initialState(): Summary.State = Summary.State.Idle

	override fun define(host: MachineHost<Summary.Effect>): MachineDefinition<Summary.State> {
		return MachineDefinition.define {
			idleTransitions(machine = this@SummaryMachine, host = host)
			loadingTransitions(machine = this@SummaryMachine, host = host)
			failedTransitions(machine = this@SummaryMachine, host = host)
			contentTransitions(machine = this@SummaryMachine, host = host)
			anyStateTransitions(machine = this@SummaryMachine, host = host)
		}
	}

	internal fun startObservation(host: MachineHost<Summary.Effect>) {
		host.launchMachineJob {
			observeUserUseCase.execute(Unit).collect { useCaseState ->
				when (useCaseState) {
					is UseCaseState.Loading -> Unit

					is UseCaseState.Data -> {
						val user = useCaseState.value
						val lastUpdateText = getString(
							Res.string.text_sync_healthy,
							user.lastUpdate.formatLastUpdate()
						)

						host.processInternalEvent(
							SummaryInternalEvent.UserObserved(
								content = with(user) {
									Summary.State.Content(
										name = toShortName(),
										lastUpdate = lastUpdateText,
										careerName = careerName,
										grade = grade.toFloat(),
										enrolledSubjects = enrolledSubjects,
										enrolledCredits = enrolledCredits,
										approvedSubjects = approvedSubjects,
										approvedCredits = approvedCredits,
										retiredSubjects = retiredSubjects,
										retiredCredits = retiredCredits,
										failedSubjects = failedSubjects,
										failedCredits = failedCredits,
										profilePictureUrl = pictureUrl,
										isProfilePictureLoading = false,
										isUserRefreshing = false
									)
								}
							)
						)
					}

					is UseCaseState.Error -> host.processInternalEvent(
						SummaryInternalEvent.ObservationFailed(
							message = getString(Res.string.snack_default_error)
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
						SummaryInternalEvent.ProfilePictureUploadStarted
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
