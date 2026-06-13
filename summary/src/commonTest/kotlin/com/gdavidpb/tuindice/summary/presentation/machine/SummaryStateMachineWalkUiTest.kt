package com.gdavidpb.tuindice.summary.presentation.machine

import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.mvi.assertMachineRandomWalk
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

/**
 * Seeded random walk for the Summary machine. UiTest-suffixed on purpose: profile
 * picture rows touch PlatformFile parsing, which the android host JVM stub cannot
 * serve, so the walk runs on the iOS simulator gate while the static validators stay
 * on the host in SummaryStateMachineContractTest.
 */
class SummaryStateMachineWalkUiTest {
	@Test
	fun machine_survivesSeededRandomWalk() = runTest {
		val userRepository = RecordingUserRepository(users = flowOf(DEFAULT_SUMMARY_USER))
		val reportingRepository = RecordingReportingRepository()

		val screenMachine = SummaryMachine(
			observeUserUseCase = ObserveUserUseCase(
				userRepository = userRepository,
				reportingRepository = reportingRepository
			),
			updateUserUseCase = UpdateUserUseCase(
				userRepository = userRepository,
				reportingRepository = reportingRepository,
				exceptionHandler = UpdateUserExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true)
				)
			),
			uploadProfilePictureUseCase = UploadProfilePictureUseCase(
				userRepository = userRepository,
				reportingRepository = reportingRepository,
				exceptionHandler = UploadProfilePictureExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true)
				)
			),
			removeProfilePictureUseCase = RemoveProfilePictureUseCase(
				userRepository = userRepository,
				reportingRepository = reportingRepository,
				exceptionHandler = RemoveProfilePictureExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true)
				)
			)
		)

		val content = Summary.State.Content(
			name = "Jane Doe",
			lastUpdate = "Hace 2 horas",
			careerName = "Ingeniería de Computación",
			grade = 4.2f,
			enrolledSubjects = 5,
			enrolledCredits = 16,
			approvedSubjects = 30,
			approvedCredits = 120,
			retiredSubjects = 1,
			retiredCredits = 4,
			failedSubjects = 2,
			failedCredits = 8,
			profilePictureUrl = "https://example.com/profile.jpg",
			isProfilePictureLoading = false,
			isUserRefreshing = false
		)

		assertMachineRandomWalk(
			screenMachine = screenMachine,
			sampleEvents = listOf(
				Summary.Action.ObserveSummary,
				Summary.Action.RefreshSummary,
				Summary.Action.TakeProfilePicture,
				Summary.Action.PickProfilePicture,
				Summary.Action.UploadProfilePicture(
					file = PlatformFile("content://profile/new.jpg")
				),
				Summary.Action.OpenProfilePictureSettings,
				Summary.Action.RemoveProfilePicture,
				Summary.Action.ConfirmRemoveProfilePicture,
				SummaryInternalEvent.UserObserved(content = content),
				SummaryInternalEvent.ObservationFailed(message = "No se pudo cargar"),
				SummaryInternalEvent.RefreshSucceeded,
				SummaryInternalEvent.RefreshFailed(message = "No se pudo actualizar"),
				SummaryInternalEvent.ProfilePictureUploadStarted,
				SummaryInternalEvent.ProfilePictureUploadSucceeded(message = "Foto actualizada"),
				SummaryInternalEvent.ProfilePictureUploadFailed(message = "No se pudo subir"),
				SummaryInternalEvent.ProfilePictureRemovalStarted,
				SummaryInternalEvent.ProfilePictureRemovalSucceeded(message = "Foto eliminada"),
				SummaryInternalEvent.ProfilePictureRemovalFailed(
					message = "No se pudo eliminar",
					clearProfilePicture = false
				)
			),
			scope = backgroundScope,
			// Conservative floor: every internal event is sampled by hand; raise to the
			// observed coverage once the walk has run on CI.
			minRowCoverage = 0.4
		)
	}
}
