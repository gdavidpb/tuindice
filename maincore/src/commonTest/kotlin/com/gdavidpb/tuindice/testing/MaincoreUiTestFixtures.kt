package com.gdavidpb.tuindice.testing

import com.gdavidpb.tuindice.base.data.source.event.NoOpEventPublisher
import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.User
import com.gdavidpb.tuindice.base.domain.repository.CredentialsRepository
import com.gdavidpb.tuindice.base.domain.repository.DeviceInfoRepository
import com.gdavidpb.tuindice.base.domain.repository.EventPublisher
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncRepository
import com.gdavidpb.tuindice.domain.usecase.GetUpdateInfoUseCase
import com.gdavidpb.tuindice.domain.usecase.RequestReviewUseCase
import com.gdavidpb.tuindice.domain.usecase.ScheduleSyncUseCase
import com.gdavidpb.tuindice.domain.usecase.SetLastMainSectionUseCase
import com.gdavidpb.tuindice.domain.usecase.StartUpUseCase
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.domain.repository.CoreCacheStateRepository
import com.gdavidpb.tuindice.presentation.machine.BrowserMachine
import com.gdavidpb.tuindice.presentation.machine.MainMachine
import com.gdavidpb.tuindice.presentation.viewmodel.BrowserViewModel
import com.gdavidpb.tuindice.presentation.viewmodel.MainViewModel
import com.gdavidpb.tuindice.summary.domain.model.ProfilePicture
import com.gdavidpb.tuindice.summary.domain.repository.UserRepository
import com.gdavidpb.tuindice.summary.domain.usecase.ObserveUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.RemoveProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UpdateUserUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.UploadProfilePictureUseCase
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.RemoveProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UpdateUserExceptionHandler
import com.gdavidpb.tuindice.summary.domain.usecase.exceptionhandler.UploadProfilePictureExceptionHandler
import com.gdavidpb.tuindice.summary.presentation.machine.SummaryMachine
import com.gdavidpb.tuindice.summary.presentation.viewmodel.SummaryViewModel
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeNetworkRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeUpdateRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.wizard.domain.usecase.ShouldStartWizardUseCase
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.flowOf

fun createBrowserViewModel(): BrowserViewModel = BrowserViewModel(
	screenMachine = BrowserMachine(),
	eventPublisher = NoOpEventPublisher
)

fun createSummaryViewModel(
	userRepository: UserRepository = FakeUserRepository()
): SummaryViewModel {
	return SummaryViewModel(
		screenMachine = SummaryMachine(
			observeUserUseCase = ObserveUserUseCase(
				userRepository = userRepository,
				reportingRepository = RecordingReportingRepository()
			),
			updateUserUseCase = UpdateUserUseCase(
				userRepository = userRepository,
				reportingRepository = RecordingReportingRepository(),
				exceptionHandler = UpdateUserExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true)
				)
			),
			uploadProfilePictureUseCase = UploadProfilePictureUseCase(
				userRepository = userRepository,
				reportingRepository = RecordingReportingRepository(),
				exceptionHandler = UploadProfilePictureExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true)
				)
			),
			removeProfilePictureUseCase = RemoveProfilePictureUseCase(
				userRepository = userRepository,
				reportingRepository = RecordingReportingRepository(),
				exceptionHandler = RemoveProfilePictureExceptionHandler(
					networkRepository = FakeNetworkRepository(isAvailable = true)
				)
			)
		),
		eventPublisher = NoOpEventPublisher
	)
}

fun createMainViewModel(
	sessionRepository: SessionRepository = FakeSessionRepository(),
	settingsRepository: FakeSettingsRepository = FakeSettingsRepository(
		reviewSuggested = true,
		lastMainSection = MainSection.SUMMARY
	),
	configRepository: FakeConfigRepository = FakeConfigRepository(),
	deviceInfoRepository: DeviceInfoRepository = FakeDeviceInfoRepository(),
	credentialsRepository: CredentialsRepository = FakeCredentialsRepository(),
	syncRepository: SyncRepository = FakeSyncRepository(),
	coreCacheStateRepository: CoreCacheStateRepository = FakeCoreCacheStateRepository(),
	updateRepository: FakeUpdateRepository = FakeUpdateRepository(),
	applicationRepository: RecordingApplicationRepository = RecordingApplicationRepository(),
	reportingRepository: RecordingReportingRepository = RecordingReportingRepository(),
	eventPublisher: EventPublisher = NoOpEventPublisher
): MainViewModel {
	return MainViewModel(
		screenMachine = MainMachine(
			startUpUseCase = StartUpUseCase(
				sessionRepository = sessionRepository,
				settingsRepository = settingsRepository,
				configRepository = configRepository,
				deviceInfoRepository = deviceInfoRepository,
				applicationRepository = applicationRepository,
				reportingRepository = reportingRepository,
				exceptionHandler = StartUpExceptionHandler()
			),
			requestReviewUseCase = RequestReviewUseCase(
				settingsRepository = settingsRepository,
				configRepository = configRepository,
				reportingRepository = reportingRepository
			),
			getUpdateInfoUseCase = GetUpdateInfoUseCase(
				configRepository = configRepository,
				updateGateway = updateRepository,
				reportingRepository = reportingRepository
			),
			scheduleSyncUseCase = ScheduleSyncUseCase(
				sessionRepository = sessionRepository,
				credentialsRepository = credentialsRepository,
				syncRepository = syncRepository,
				coreCacheStateRepository = coreCacheStateRepository,
				reportingRepository = reportingRepository
			),
			setLastMainSectionUseCase = SetLastMainSectionUseCase(
				settingsRepository = settingsRepository,
				reportingRepository = reportingRepository
			),
			shouldStartWizardUseCase = ShouldStartWizardUseCase(
				settingsRepository = settingsRepository,
				sessionRepository = sessionRepository,
				reportingRepository = reportingRepository
			)
		),
		eventPublisher = eventPublisher
	)
}

class FakeCoreCacheStateRepository(
	private val requiresBaseRehydration: Boolean = false
) : CoreCacheStateRepository {
	override suspend fun requiresBaseRehydration(): Boolean = requiresBaseRehydration
}

class FakeDeviceInfoRepository(
	private val hasCamera: Boolean = false,
	private val versionName: String = "1.0.0",
	private val versionCode: Long = 1L
) : DeviceInfoRepository {
	override fun appVersionName(): String = versionName

	override fun appVersionCode(): Long = versionCode

	override fun hasCamera(): Boolean = hasCamera
}

private class FakeUserRepository(
	private val user: User = User(
		id = "1",
		cid = "cid-1",
		usbId = "20261234",
		email = "ana@tuindice.app",
		pictureUrl = "https://cdn.tuindice.app/profile.jpg",
		fullName = "Ana Diaz",
		firstNames = "Ana",
		lastNames = "Diaz",
		careerName = "Ingenieria Informatica",
		careerCode = 123,
		scholarship = false,
		grade = 4.25,
		enrolledSubjects = 5,
		enrolledCredits = 21,
		approvedSubjects = 3,
		approvedCredits = 18,
		retiredSubjects = 1,
		retiredCredits = 2,
		failedSubjects = 1,
		failedCredits = 1,
		lastUpdate = 1_742_437_200_000L
	),
	private val profilePicture: ProfilePicture = ProfilePicture(
		url = "https://cdn.tuindice.app/profile.jpg"
	)
) : UserRepository {
	override suspend fun observeUserFlow() = flowOf(user)

	override suspend fun updateUser() = Unit

	override suspend fun uploadProfilePicture(file: PlatformFile): ProfilePicture = profilePicture

	override suspend fun removeProfilePicture() = Unit
}
