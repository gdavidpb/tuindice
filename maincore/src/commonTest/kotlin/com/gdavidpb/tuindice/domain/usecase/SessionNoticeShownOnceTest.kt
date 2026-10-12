package com.gdavidpb.tuindice.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.auth.domain.model.BootstrapTokens
import com.gdavidpb.tuindice.auth.domain.model.RefreshTokens
import com.gdavidpb.tuindice.auth.domain.repository.AuthRepository
import com.gdavidpb.tuindice.base.data.source.SessionInvalidationDataSource
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.data.repository.sync.FakeSyncStatusRepository
import com.gdavidpb.tuindice.data.source.session.SessionRecoveryDataSource
import com.gdavidpb.tuindice.domain.model.SessionInvalidationReason
import com.gdavidpb.tuindice.domain.usecase.exceptionhandler.StartUpExceptionHandler
import com.gdavidpb.tuindice.domain.usecase.result.StartUpResult
import com.gdavidpb.tuindice.security.domain.model.Attestation
import com.gdavidpb.tuindice.security.domain.model.AttestationRequest
import com.gdavidpb.tuindice.security.domain.repository.AttestationRepository
import com.gdavidpb.tuindice.testing.FakeDeviceInfoRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeConfigRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingApplicationRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

// The user sees the notice of a session that ended behind their back ONCE, on the first occasion
// the interface can show it: whether the app stayed alive (the live signal, the interface
// acknowledging it) or the process died (the start-up reading what the invalidation left).
class SessionNoticeShownOnceTest {
	// The app stays alive: the live signal reaches the interface, which shows it and acknowledges it;
	// the next start has nothing left to say.
	@Test
	fun whenTheAppStaysAlive_theLiveNoticeIsShownOnce_andTheNextStartHasNothingToSay() = runTest {
		val world = World(this)

		world.invalidate()
		world.showLiveNotice(expected = true)

		assertFalse(world.settings.sessionResetNoticePending, "and the acknowledgement took the copy kept for the start")
		assertFalse(world.nextStart().showSessionResetNotice, "so the next start does not show it again")
	}

	// The process dies before the interface could show anything: the notice was kept, and the next
	// start shows it once.
	@Test
	fun whenTheProcessDies_theNextStartShowsTheNoticeOnce() = runTest {
		val world = World(this)

		world.invalidate()
		// The process is gone: the memory goes with it, the settings stay.
		val afterTheDeath = world.afterAProcessDeath()

		assertTrue(afterTheDeath.nextStart().showSessionResetNotice, "the next start explains the sign-in screen")
		assertFalse(afterTheDeath.nextStart().showSessionResetNotice, "and only once")
	}

	// The signal reached the machine but the process was killed before the screen showed it (a
	// background kill) and nothing was acknowledged: it is not lost.
	@Test
	fun whenTheProcessDiesAfterTheLiveSignalButBeforeItWasShown_theNextStartStillShowsIt() = runTest {
		val world = World(this)

		world.invalidate()
		world.receiveLiveNoticeWithoutShowingIt()
		val afterTheDeath = world.afterAProcessDeath()

		assertTrue(afterTheDeath.nextStart().showSessionResetNotice)
	}

	// The invalidation happened while the start-up was still resolving: the start-up showed the
	// notice (it read what the invalidation left), and the live signal that arrives after the
	// content is up is the same notice, not a second one.
	@Test
	fun whenTheStartUpShowedTheNotice_theLiveSignalThatArrivesAfterwardsIsNotShownAgain() = runTest {
		val world = World(this)

		world.invalidate()
		assertTrue(world.nextStart().showSessionResetNotice)

		// The user was told already: the signal that arrives afterwards says nothing.
		world.showLiveNotice(expected = false)
	}

	// The notice reaches the interface when the content that observes it is up, however late: the
	// signal waits for it, once.
	@Test
	fun whenTheInvalidationComesBeforeTheInterfaceObserves_theSignalWaitsForItAndIsDeliveredOnce() = runTest {
		val world = World(this)

		world.invalidate()
		world.showLiveNotice(expected = true)
		// The signal was delivered, not kept for every observer.
		world.showLiveNotice(expected = false)
	}

	private class World(
		private val testScope: TestScope,
		val settings: FakeSettingsRepository = FakeSettingsRepository(),
		private val sessionInvalidations: SessionInvalidationRepository = SessionInvalidationDataSource()
	) {
		private val reporting = RecordingReportingRepository()
		private val sessionRepository = FakeSessionRepository(
			sessionId = "session-old",
			usbId = "12-34567",
			accessToken = "access-old",
			refreshToken = "refresh-old"
		)

		suspend fun invalidate() {
			SessionRecoveryDataSource(
				sessionRepository = sessionRepository,
				applicationRepository = RecordingApplicationRepository(),
				sessionInvalidationRepository = sessionInvalidations,
				syncStatusRepository = FakeSyncStatusRepository(),
				attestationRepository = object : AttestationRepository {
					override suspend fun attest(request: AttestationRequest) = Attestation(token = "token")
				},
				authRepository = UnusedAuthRepository,
				credentialsRepository = FakeCredentialsRepository(),
				settingsRepository = settings,
				reportingRepository = reporting,
				sessionCoroutineScope = testSessionCoroutineScope(StandardTestDispatcher(testScope.testScheduler))
			).invalidateSession(sessionId = "session-old", reason = SessionInvalidationReason.RefreshRejected)
		}

		// The same settings (they live on the disk) and a session that is gone, with a new process: the
		// memory of the old one, the signal included, is not there.
		fun afterAProcessDeath() = World(
			testScope = testScope,
			settings = settings,
			sessionInvalidations = SessionInvalidationDataSource()
		)

		suspend fun nextStart(): StartUpResult.Available {
			var result: StartUpResult.Available? = null

			StartUpUseCase(
				sessionRepository = FakeSessionRepository(sessionId = ""),
				settingsRepository = settings,
				configRepository = FakeConfigRepository(),
				deviceInfoRepository = FakeDeviceInfoRepository(),
				applicationRepository = RecordingApplicationRepository(),
				reportingRepository = reporting,
				exceptionHandler = StartUpExceptionHandler()
			).execute(Unit).test {
				assertIs<UseCaseState.Loading>(awaitItem())
				result = assertIs<StartUpResult.Available>(assertIs<UseCaseState.Data<StartUpResult>>(awaitItem()).value)
				awaitComplete()
			}

			return checkNotNull(result)
		}

		// What the content does when it is up: it observes the signal and, for each one, shows the
		// notice and acknowledges it. [expected] says whether a signal is waiting for it.
		suspend fun showLiveNotice(expected: Boolean) {
			observeLiveNotice(expected) {
				AcknowledgeSessionInvalidationUseCase(settings, reporting).execute(Unit).test {
					cancelAndIgnoreRemainingEvents()
				}
			}
		}

		// The signal reaches the machine, and the process dies before the interface shows it.
		suspend fun receiveLiveNoticeWithoutShowingIt() {
			observeLiveNotice(expected = true) { }
		}

		private suspend fun observeLiveNotice(expected: Boolean, onNotice: suspend () -> Unit) {
			ObserveSessionInvalidationUseCase(sessionInvalidations, settings, reporting).execute(Unit).test {
				assertIs<UseCaseState.Loading>(awaitItem())

				if (expected) {
					assertEquals(UseCaseState.Data(Unit), awaitItem())
					onNotice()
				} else {
					expectNoEvents()
				}

				cancelAndIgnoreRemainingEvents()
			}
		}
	}
}

private object UnusedAuthRepository : AuthRepository {
	override suspend fun bootstrapSignIn(usbId: String, password: String): BootstrapTokens = error("unused")

	override suspend fun exchangeSignIn(bootstrapAccessToken: String, attestation: Attestation) = error("unused")

	override suspend fun reissueTokens(usbId: String, password: String, attestation: Attestation) = error("unused")

	override suspend fun refreshTokens(sessionId: String, refreshToken: String, attestation: Attestation): RefreshTokens =
		error("unused")

	override suspend fun revokeTokens(sessionId: String, refreshToken: String, attestation: Attestation) =
		error("unused")
}
