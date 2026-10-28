package com.gdavidpb.tuindice.summary.presentation.viewmodel

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.dispatcher.TuIndiceDispatchers
import com.gdavidpb.tuindice.summary.domain.usecase.GetCameraAvailabilityUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveSyncUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.SummaryObservations
import com.gdavidpb.tuindice.summary.domain.usecase.SummaryTermReports
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine
import com.gdavidpb.tuindice.summary.testing.ControllableAssistantInsightsRepository
import com.gdavidpb.tuindice.summary.testing.ControllableTermReportSeenRepository
import com.gdavidpb.tuindice.summary.testing.DEFAULT_SUMMARY_USER
import com.gdavidpb.tuindice.summary.testing.RecordingUserRepository
import com.gdavidpb.tuindice.summary.testing.createObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.testing.createSummaryTermReports
import com.gdavidpb.tuindice.summary.testing.termReportInsight
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.withMainDispatcher
import com.gdavidpb.tuindice.testkit.mvi.launchStateCollector
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test

// The line of the term reports on the summary: an unread report puts its line there, and opening the report takes the
// line away again.
class SummaryTermReportLineViewModelTest {
	@Test
	@OptIn(ExperimentalCoroutinesApi::class)
	fun unreadTermReport_putsItsLineOnTheSummary_andOpeningItTakesTheLineAway() = runTest {
		withMainDispatcher { dispatchers ->
			val seen = ControllableTermReportSeenRepository()
			val viewModel = createViewModel(
				dispatchers = dispatchers,
				termReports = createSummaryTermReports(
					insights = ControllableAssistantInsightsRepository(listOf(termReportInsight(id = "term-1:2"))),
					seen = seen
				)
			)
			backgroundScope.launchStateCollector(flow = viewModel.state, testScheduler = testScheduler)

			withTimeout(TIMEOUT_MILLIS) { viewModel.state.first { state -> state.hub.termReport != null } }

			seen.markTermReportSeen(insightId = "term-1:2")

			withTimeout(TIMEOUT_MILLIS) { viewModel.state.first { state -> state.hub.termReport == null } }
		}
	}

	private fun createViewModel(
		dispatchers: TuIndiceDispatchers,
		termReports: SummaryTermReports
	): SummaryViewModel {
		val userRepository = RecordingUserRepository(users = flowOf(DEFAULT_SUMMARY_USER))
		val reporting = RecordingReportingRepository()
		val network = FakeNetworkRepository(isAvailable = true)

		return SummaryViewModel(
			screenMachine = SummaryMachine(
				observations = SummaryObservations(
					user = ObserveUserUseCase(userRepository = userRepository, reportingRepository = reporting),
					sync = ObserveSyncUseCase(
						syncStatusRepository = FakeSyncStatusRepository(),
						syncRepository = FakeSyncRepository(),
						reportingRepository = reporting
					),
					hub = createObserveSummaryHubUseCase()
				),
				getCameraAvailabilityUseCase = GetCameraAvailabilityUseCase(
					deviceInfoRepository = FakeDeviceInfoRepository(),
					reportingRepository = reporting
				),
				updateUserUseCase = UpdateUserUseCase(
					userRepository = userRepository,
					reportingRepository = reporting,
					exceptionHandler = UpdateUserExceptionHandler(networkRepository = network)
				),
				uploadProfilePictureUseCase = UploadProfilePictureUseCase(
					userRepository = userRepository,
					reportingRepository = reporting,
					exceptionHandler = UploadProfilePictureExceptionHandler(networkRepository = network)
				),
				termReports = termReports,
				removeProfilePictureUseCase = RemoveProfilePictureUseCase(
					userRepository = userRepository,
					reportingRepository = reporting,
					exceptionHandler = RemoveProfilePictureExceptionHandler(networkRepository = network)
				)
			),
			eventPublisher = NoOpEventPublisher,
			dispatchers = dispatchers
		)
	}

	private companion object {
		const val TIMEOUT_MILLIS = 3_000L
	}
}
