package com.gdavidpb.tuindice.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.domain.usecase.result.StartUpResult
import com.gdavidpb.tuindice.testing.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class StartUpUseCaseTest {
	@Test
	fun execute_returnsAppUnavailableWhenRemoteConfigNoticeIsEnabled() = runTest {
		val notice = AppAvailabilityNotice(
			enabled = true,
			title = "Servicio pausado",
			message = "Estamos en mantenimiento."
		)
		val useCase = StartUpUseCase(
			sessionRepository = FailingSessionRepository(),
			settingsRepository = FakeSettingsRepository(),
			configRepository = FakeConfigRepository(appAvailabilityNotice = notice),
			deviceInfoRepository = FakeDeviceInfoRepository(),
			applicationRepository = RecordingApplicationRepository(),
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = StartUpExceptionHandler()
		)

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())

			val data = assertIs<UseCaseState.Data<StartUpResult>>(awaitItem())
			val result = assertIs<StartUpResult.AppUnavailable>(data.value)
			assertEquals(notice, result.notice)

			awaitComplete()
		}
	}

	@Test
	fun execute_returnsOutdatedAppWhenPersistedMinimumVersionIsGreaterThanInstalledVersion() = runTest {
		val outdatedAppState = OutdatedAppState(minimumVersionCode = 52)
		val useCase = StartUpUseCase(
			sessionRepository = FailingSessionRepository(),
			settingsRepository = FakeSettingsRepository(outdatedAppState = outdatedAppState),
			configRepository = FakeConfigRepository(),
			deviceInfoRepository = FakeDeviceInfoRepository(versionCode = 51),
			applicationRepository = RecordingApplicationRepository(),
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = StartUpExceptionHandler()
		)

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())

			val data = assertIs<UseCaseState.Data<StartUpResult>>(awaitItem())
			val result = assertIs<StartUpResult.OutdatedApp>(data.value)
			assertEquals(outdatedAppState, result.state)

			awaitComplete()
		}
	}

	@Test
	fun execute_clearsOutdatedAppStateWhenInstalledVersionSatisfiesMinimumVersion() = runTest {
		val settingsRepository = FakeSettingsRepository(
			outdatedAppState = OutdatedAppState(minimumVersionCode = 52)
		)
		val useCase = StartUpUseCase(
			sessionRepository = FakeSessionRepository(),
			settingsRepository = settingsRepository,
			configRepository = FakeConfigRepository(),
			deviceInfoRepository = FakeDeviceInfoRepository(versionCode = 52),
			applicationRepository = RecordingApplicationRepository(),
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = StartUpExceptionHandler()
		)

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())
			assertIs<UseCaseState.Data<StartUpResult>>(awaitItem())
			awaitComplete()
		}

		assertEquals(null, settingsRepository.getOutdatedAppState())
	}

	private class FailingSessionRepository : SessionRepository {
		override suspend fun hasActiveSession(): Boolean {
			error("Session should not be resolved when the app availability notice is enabled.")
		}

		override suspend fun getActiveSessionSnapshot(): SessionSnapshot? = error("Unexpected session read.")
		override suspend fun setSessionSnapshot(snapshot: SessionSnapshot) = error("Unexpected session write.")
		override suspend fun replaceSessionSnapshotIfCurrent(
			expectedSnapshot: SessionSnapshot,
			newSnapshot: SessionSnapshot
		): Boolean = error("Unexpected session write.")
		override suspend fun setUsbId(usbId: String) = error("Unexpected session write.")
		override suspend fun setSessionId(sessionId: String) = error("Unexpected session write.")
		override suspend fun setAccessToken(accessToken: String) = error("Unexpected session write.")
		override suspend fun setRefreshToken(refreshToken: String) = error("Unexpected session write.")
		override suspend fun getUsbId(): String = error("Unexpected session read.")
		override suspend fun getSessionId(): String = error("Unexpected session read.")
		override suspend fun getAccessToken(): String = error("Unexpected session read.")
		override suspend fun getRefreshToken(): String = error("Unexpected session read.")
		override suspend fun clear() = error("Unexpected session clear.")
	}
}
