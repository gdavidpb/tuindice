package com.gdavidpb.tuindice.summary.testing

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.GetCameraAvailabilityUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveSyncUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.SummaryObservations
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.contract.Summary
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine
import com.gdavidpb.tuindice.summary.presentation.model.SummaryHubItem
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import kotlin.test.assertIs
import kotlin.test.assertNotNull

val hubScenarioRecord = hubRecord(
	hubTerm(
		attempts = listOf(
			hubAttempt(code = "CI5437", schedule = listOf(hubMeeting(day = HUB_THURSDAY, start = 3, end = 4)))
		)
	)
)

// A Summary view model whose stored user is [users] (none when null) and whose hub has a class in
// progress on Thursday at ten, over [pensum]. A refresh fails with [refreshFailure], and waits for
// [refreshGate] first when it is given: with the unconfined dispatchers of the tests a refresh that
// ends at once is over before the state that started it is set.
fun createHubScenarioViewModel(
	dispatchers: TuIndiceDispatchers,
	users: Flow<User>? = MutableStateFlow(DEFAULT_SUMMARY_USER),
	pensum: ControllablePensumProgressRepository = ControllablePensumProgressRepository(),
	refreshFailure: Throwable? = null,
	refreshGate: CompletableDeferred<Unit>? = null
): SummaryViewModel {
	val recordingRepository = RecordingUserRepository(
		users = users ?: emptyFlow(),
		updateThrowable = refreshFailure
	)
	// The refresh waits for the test to let it go, when the test needs to look at it running.
	val userRepository: UserRepository = if (refreshGate == null) {
		recordingRepository
	} else {
		object : UserRepository by recordingRepository {
			override suspend fun updateUser() {
				refreshGate.await()
				recordingRepository.updateUser()
			}
		}
	}
	val reporting = RecordingReportingRepository()

	return SummaryViewModel(
		screenMachine = SummaryMachine(
			observations = SummaryObservations(
				user = ObserveUserUseCase(userRepository, reporting),
				sync = ObserveSyncUseCase(FakeSyncStatusRepository(), FakeSyncRepository(), reporting),
				hub = createObserveSummaryHubUseCase(
					record = ControllableVisibleAcademicRecordRepository(hubScenarioRecord),
					pensumProgress = pensum
				)
			),
			getCameraAvailabilityUseCase = GetCameraAvailabilityUseCase(FakeDeviceInfoRepository(), reporting),
			updateUserUseCase = UpdateUserUseCase(
				userRepository = userRepository,
				reportingRepository = reporting,
				exceptionHandler = UpdateUserExceptionHandler(FakeNetworkRepository(isAvailable = true))
			),
			uploadProfilePictureUseCase = UploadProfilePictureUseCase(
				userRepository = userRepository,
				reportingRepository = reporting,
				exceptionHandler = UploadProfilePictureExceptionHandler(FakeNetworkRepository(isAvailable = true))
			),
			removeProfilePictureUseCase = RemoveProfilePictureUseCase(
				userRepository = userRepository,
				reportingRepository = reporting,
				exceptionHandler = RemoveProfilePictureExceptionHandler(FakeNetworkRepository(isAvailable = true))
			)
		),
		eventPublisher = NoOpEventPublisher,
		dispatchers = dispatchers
	)
}

// The hub is on the state while the user has not spoken: the content cannot be the one that shows it.
suspend fun SummaryViewModel.awaitHubOutsideTheContent(): SummaryHubItem {
	return withTimeout(3_000) {
		state.first { current -> current !is Summary.State.Content && current.hub.career != null }.hub
	}.also { hub ->
		assertNotNull(hub.week)
		assertIs<Summary.State.Idle>(state.value)
	}
}

suspend fun SummaryViewModel.awaitContent(
	predicate: (Summary.State.Content) -> Boolean
): Summary.State.Content {
	return withTimeout(3_000) {
		state.filterIsInstance<Summary.State.Content>().first { content -> predicate(content) }
	}
}
