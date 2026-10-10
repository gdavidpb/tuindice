package com.gdavidpb.tuindice.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.domain.model.StartUpTarget
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

	@Test
	fun execute_setsSessionResetNoticeWhenSessionReadFailsAndDataIsCleared() = runTest {
		val settingsRepository = FakeSettingsRepository()
		val applicationRepository = RecordingApplicationRepository()
		val useCase = StartUpUseCase(
			sessionRepository = FailingSessionRepository(),
			settingsRepository = settingsRepository,
			configRepository = FakeConfigRepository(),
			deviceInfoRepository = FakeDeviceInfoRepository(),
			applicationRepository = applicationRepository,
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = StartUpExceptionHandler()
		)

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())
			assertIs<UseCaseState.Error<*>>(awaitItem())
			awaitComplete()
		}

		assertEquals(true, settingsRepository.sessionResetNoticePending)
	}

	@Test
	fun execute_consumesSessionResetNoticeAndFlagsAuthStart() = runTest {
		val settingsRepository = FakeSettingsRepository()
		settingsRepository.setSessionResetNoticePending()

		val useCase = StartUpUseCase(
			sessionRepository = FakeSessionRepository(sessionId = ""),
			settingsRepository = settingsRepository,
			configRepository = FakeConfigRepository(),
			deviceInfoRepository = FakeDeviceInfoRepository(),
			applicationRepository = RecordingApplicationRepository(),
			reportingRepository = RecordingReportingRepository(),
			exceptionHandler = StartUpExceptionHandler()
		)

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())

			val data = assertIs<UseCaseState.Data<StartUpResult>>(awaitItem())
			val result = assertIs<StartUpResult.Available>(data.value)
			assertEquals(true, result.showSessionResetNotice)

			awaitComplete()
		}

		assertEquals(false, settingsRepository.sessionResetNoticePending)
	}

	// What a previous version left behind for the account that signed out belongs to nobody now.
	@Test
	fun execute_withoutASession_clearsTheSessionResidueOnce() = runTest {
		val applicationRepository = RecordingApplicationRepository()

		val result = runStartUp(
			startUpUseCase(
				sessionRepository = FakeSessionRepository(sessionId = ""),
				applicationRepository = applicationRepository
			)
		)

		assertEquals(StartUpTarget.Auth, assertIs<StartUpResult.Available>(result).startTarget)
		assertEquals(1, applicationRepository.clearSessionResidueCalls)
		assertEquals(0, applicationRepository.clearCalls)
	}

	// With a session the saved files are the user's own.
	@Test
	fun execute_withASession_keepsTheSessionResidue() = runTest {
		val applicationRepository = RecordingApplicationRepository()

		val result = runStartUp(
			startUpUseCase(
				sessionRepository = FakeSessionRepository(),
				applicationRepository = applicationRepository
			)
		)

		assertIs<StartUpTarget.Main>(assertIs<StartUpResult.Available>(result).startTarget)
		assertEquals(0, applicationRepository.clearSessionResidueCalls)
	}

	@Test
	fun execute_whenClearingTheSessionResidueFails_startsTheSameAndReportsIt() = runTest {
		val failure = IllegalStateException("residue failed")
		val settingsRepository = FakeSettingsRepository()
		val applicationRepository = RecordingApplicationRepository(clearSessionResidueFailure = failure)
		val reportingRepository = RecordingReportingRepository()

		val result = runStartUp(
			startUpUseCase(
				sessionRepository = FakeSessionRepository(sessionId = ""),
				applicationRepository = applicationRepository,
				settingsRepository = settingsRepository,
				reportingRepository = reportingRepository
			)
		)

		assertEquals(StartUpTarget.Auth, assertIs<StartUpResult.Available>(result).startTarget)
		assertEquals(listOf<Throwable>(failure), reportingRepository.loggedExceptions)
		// It is not a broken session: nothing is wiped and no notice is left.
		assertEquals(0, applicationRepository.clearCalls)
		assertEquals(false, settingsRepository.sessionResetNoticePending)
	}

	// The session is not decided on these two paths, so the residue is left for the next start.
	@Test
	fun execute_whenTheAppIsOutdated_doesNotClearTheSessionResidue() = runTest {
		val applicationRepository = RecordingApplicationRepository()

		val result = runStartUp(
			StartUpUseCase(
				sessionRepository = FailingSessionRepository(),
				settingsRepository = FakeSettingsRepository(outdatedAppState = OutdatedAppState(minimumVersionCode = 52)),
				configRepository = FakeConfigRepository(),
				deviceInfoRepository = FakeDeviceInfoRepository(versionCode = 51),
				applicationRepository = applicationRepository,
				reportingRepository = RecordingReportingRepository(),
				exceptionHandler = StartUpExceptionHandler()
			)
		)

		assertIs<StartUpResult.OutdatedApp>(result)
		assertEquals(0, applicationRepository.clearSessionResidueCalls)
	}

	@Test
	fun execute_whenTheAppIsUnavailable_doesNotClearTheSessionResidue() = runTest {
		val applicationRepository = RecordingApplicationRepository()

		val result = runStartUp(
			StartUpUseCase(
				sessionRepository = FailingSessionRepository(),
				settingsRepository = FakeSettingsRepository(),
				configRepository = FakeConfigRepository(
					appAvailabilityNotice = AppAvailabilityNotice(enabled = true, title = "Pausa", message = "Mantenimiento.")
				),
				deviceInfoRepository = FakeDeviceInfoRepository(),
				applicationRepository = applicationRepository,
				reportingRepository = RecordingReportingRepository(),
				exceptionHandler = StartUpExceptionHandler()
			)
		)

		assertIs<StartUpResult.AppUnavailable>(result)
		assertEquals(0, applicationRepository.clearSessionResidueCalls)
	}

	private fun startUpUseCase(
		sessionRepository: SessionRepository,
		applicationRepository: RecordingApplicationRepository,
		settingsRepository: FakeSettingsRepository = FakeSettingsRepository(),
		reportingRepository: RecordingReportingRepository = RecordingReportingRepository()
	) = StartUpUseCase(
		sessionRepository = sessionRepository,
		settingsRepository = settingsRepository,
		configRepository = FakeConfigRepository(),
		deviceInfoRepository = FakeDeviceInfoRepository(),
		applicationRepository = applicationRepository,
		reportingRepository = reportingRepository,
		exceptionHandler = StartUpExceptionHandler()
	)

	private suspend fun runStartUp(useCase: StartUpUseCase): StartUpResult {
		var result: StartUpResult? = null

		useCase.execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())
			result = assertIs<UseCaseState.Data<StartUpResult>>(awaitItem()).value
			awaitComplete()
		}

		return checkNotNull(result)
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
