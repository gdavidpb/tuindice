package com.gdavidpb.tuindice.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.domain.model.AppAvailabilityNotice
import com.gdavidpb.tuindice.base.domain.model.OutdatedAppState
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
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

		// The reason is the true one: the data could not be read, not that the server ended anything.
		assertEquals(SessionResetNoticeReason.UnreadableData, settingsRepository.sessionResetNoticeReason)
	}

	@Test
	fun execute_offersTheSessionResetNoticeWithoutTakingIt() = runTest {
		val settingsRepository = FakeSettingsRepository()
		settingsRepository.setSessionResetNoticePending(SessionResetNoticeReason.ServerEndedSession)

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
			assertEquals(SessionResetNoticeReason.ServerEndedSession, result.sessionResetNotice?.reason)

			awaitComplete()
		}

		// Reading it is not showing it: it stays until the interface says it showed it.
		assertEquals(true, settingsRepository.sessionResetNoticePending)
	}

	// A notice left for a session that came back (there are tokens again) explains nothing: it is
	// dropped, not kept for a later sign-out.
	@Test
	fun execute_withASession_dropsAStaleSessionResetNotice() = runTest {
		val settingsRepository = FakeSettingsRepository()
		settingsRepository.setSessionResetNoticePending(SessionResetNoticeReason.ServerEndedSession)

		val result = runStartUp(
			startUpUseCase(
				sessionRepository = FakeSessionRepository(),
				settingsRepository = settingsRepository
			)
		)

		assertNull(assertIs<StartUpResult.Available>(result).sessionResetNotice)
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

	// Reading the stored session can end it (a half-written one is dropped by the store, which persists the
	// notice that explains it while it is read): the notice is read after, or the sign-in this start
	// lands on would go unexplained.
	@Test
	fun execute_readsTheSessionBeforeTheNotice_soTheNoticePersistedWhileReadingTheSessionIsOffered() = runTest {
		val settingsRepository = FakeSettingsRepository()
		val sessionRepository = object : SessionRepository by FakeSessionRepository(sessionId = "") {
			override suspend fun hasActiveSession(): Boolean {
				settingsRepository.setSessionResetNoticePending(SessionResetNoticeReason.UnreadableData)

				return false
			}
		}

		val result = runStartUp(
			startUpUseCase(sessionRepository = sessionRepository, settingsRepository = settingsRepository)
		)

		assertEquals(
			SessionResetNoticeReason.UnreadableData,
			assertIs<StartUpResult.Available>(result).sessionResetNotice?.reason
		)
	}

	// A start that is cancelled while it reads the session (the screen closed) found nothing broken: it must
	// not wipe the data the next start will read.
	@Test
	fun execute_whenTheStartIsCancelledWhileReadingTheSession_clearsNothing() = runTest {
		val applicationRepository = RecordingApplicationRepository()
		val settingsRepository = FakeSettingsRepository()
		lateinit var start: Job
		val sessionRepository = object : SessionRepository by FakeSessionRepository() {
			override suspend fun hasActiveSession(): Boolean {
				start.cancel()
				yield()

				return true
			}
		}
		val useCase = startUpUseCase(
			sessionRepository = sessionRepository,
			applicationRepository = applicationRepository,
			settingsRepository = settingsRepository
		)

		start = launch { useCase.execute(Unit).collect() }
		advanceUntilIdle()

		assertTrue(start.isCancelled)
		assertEquals(0, applicationRepository.clearCalls, "a cancellation is not a session that cannot be read")
		assertNull(settingsRepository.sessionResetNoticeReason)
	}

	private fun startUpUseCase(
		sessionRepository: SessionRepository,
		applicationRepository: RecordingApplicationRepository = RecordingApplicationRepository(),
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
		override suspend fun getUsbId(): String = error("Unexpected session read.")
		override suspend fun getSessionId(): String = error("Unexpected session read.")
		override suspend fun getAccessToken(): String = error("Unexpected session read.")
		override suspend fun getRefreshToken(): String = error("Unexpected session read.")
		override suspend fun clear() = error("Unexpected session clear.")
	}
}
