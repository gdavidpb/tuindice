package com.gdavidpb.tuindice.domain.usecase

import app.cash.turbine.test
import com.gdavidpb.tuindice.base.data.source.SessionInvalidationDataSource
import com.gdavidpb.tuindice.base.domain.model.SessionResetNotice
import com.gdavidpb.tuindice.base.domain.model.SessionResetNoticeReason
import com.gdavidpb.tuindice.base.domain.usecase.base.UseCaseState
import com.gdavidpb.tuindice.base.utils.PreferencesKeys
import com.gdavidpb.tuindice.testing.RealSessionStore
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

// A session found half written (or unreadable) ends from the store itself, with nobody to ask. It must
// carry its notice like the one that comes from the server does: the sign-in screen that follows is
// explained live, and at the next start if nobody collected the signal.
class StoreOriginInvalidationNoticeTest {
	@Test
	fun aSessionFoundHalfWritten_isSignalledWithItsNotice_andLeavesItForTheNextStart() = runTest {
		val settings = FakeSettingsRepository()
		val invalidations = SessionInvalidationDataSource()
		val store = RealSessionStore(settings = settings, invalidations = invalidations)
		store.active.values[PreferencesKeys.USER_SESSION_ID] = "session-old"
		store.active.values[PreferencesKeys.USER_ACCESS_TOKEN] = "access-old"

		assertNull(store.repository.getActiveSessionSnapshot(), "the half-written session is dropped")

		ObserveSessionInvalidationUseCase(invalidations, settings, RecordingReportingRepository()).execute(Unit).test {
			assertIs<UseCaseState.Loading>(awaitItem())
			val signal = assertIs<UseCaseState.Data<SessionResetNotice?>>(awaitItem())

			assertEquals(SessionResetNoticeReason.UnreadableData, signal.value?.reason, "the signal carries the notice")
			assertEquals(signal.value, settings.getSessionResetNotice(), "and it is kept for the next start")
			cancelAndIgnoreRemainingEvents()
		}
	}

	// An intentional sign-out suppresses the signal and takes its notice back: nobody is owed an
	// explanation of what the user did.
	@Test
	fun whenTheSignalIsSuppressed_theNoticeIsTakenBackByItsStamp() = runTest {
		val settings = FakeSettingsRepository()
		val invalidations = SessionInvalidationDataSource()
		val store = RealSessionStore(settings = settings, invalidations = invalidations)
		store.active.values[PreferencesKeys.USER_SESSION_ID] = "session-old"
		invalidations.markIntentionalSignOut("session-old")

		assertNull(store.repository.getActiveSessionSnapshot())

		assertNull(settings.getSessionResetNotice(), "the notice that explained nothing was taken back")
	}
}
