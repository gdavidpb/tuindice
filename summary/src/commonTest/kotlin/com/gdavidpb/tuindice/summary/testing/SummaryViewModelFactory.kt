package com.gdavidpb.tuindice.summary.testing

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.summary.domain.usecase.GetCameraAvailabilityUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveSummaryHubUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveSyncUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.SummaryObservations
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.TestTuIndiceDispatchers
import kotlinx.coroutines.Dispatchers

/** A Summary view model for the stored user, with [hub] saying what the hub says; the rest is quiet. */
fun createSummaryViewModelWithHub(hub: ObserveSummaryHubUseCase): SummaryViewModel {
	val userRepository = RecordingUserRepository()
	val reporting = RecordingReportingRepository()
	val network = FakeNetworkRepository(isAvailable = true)

	return SummaryViewModel(
		screenMachine = SummaryMachine(
			observations = SummaryObservations(
				user = ObserveUserUseCase(userRepository, reporting),
				sync = ObserveSyncUseCase(FakeSyncStatusRepository(), FakeSyncRepository(), reporting),
				hub = hub
			),
			getCameraAvailabilityUseCase = GetCameraAvailabilityUseCase(FakeDeviceInfoRepository(), reporting),
			updateUserUseCase = UpdateUserUseCase(
				userRepository = userRepository,
				reportingRepository = reporting,
				exceptionHandler = UpdateUserExceptionHandler(network)
			),
			uploadProfilePictureUseCase = UploadProfilePictureUseCase(
				userRepository = userRepository,
				reportingRepository = reporting,
				exceptionHandler = UploadProfilePictureExceptionHandler(network)
			),
			termReports = createSummaryTermReports(),
			removeProfilePictureUseCase = RemoveProfilePictureUseCase(
				userRepository = userRepository,
				reportingRepository = reporting,
				exceptionHandler = RemoveProfilePictureExceptionHandler(network)
			)
		),
		eventPublisher = NoOpEventPublisher,
		dispatchers = TestTuIndiceDispatchers(Dispatchers.Unconfined)
	)
}
