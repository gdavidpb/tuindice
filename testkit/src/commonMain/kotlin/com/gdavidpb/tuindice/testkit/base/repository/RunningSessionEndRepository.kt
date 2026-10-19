package com.gdavidpb.tuindice.testkit.base.repository

import com.gdavidpb.tuindice.base.domain.coroutine.SessionCoroutineScope
import com.gdavidpb.tuindice.base.domain.repository.ApplicationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionEndRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionInvalidationRepository
import com.gdavidpb.tuindice.base.domain.repository.SessionRepository
import com.gdavidpb.tuindice.base.domain.repository.SyncStatusRepository
import com.gdavidpb.tuindice.testkit.coroutines.testSessionCoroutineScope

// The local end of a sign-out as the use case needs it, without the lock and the reporting of the real
// one: the steps run in order over the doubles it is given, so a test reads their effects.
class RunningSessionEndRepository(
	private val applicationRepository: ApplicationRepository = RecordingApplicationRepository(),
	private val sessionRepository: SessionRepository = FakeSessionRepository(),
	private val syncStatusRepository: SyncStatusRepository = FakeSyncStatusRepository(),
	private val sessionCoroutineScope: SessionCoroutineScope = testSessionCoroutineScope(),
	private val sessionInvalidationRepository: SessionInvalidationRepository = FakeSessionInvalidationRepository()
) : SessionEndRepository {
	override suspend fun endSignedOutSession(sessionId: String) {
		sessionCoroutineScope.cancelActiveWork()
		applicationRepository.clearData()
		sessionRepository.clear()
		syncStatusRepository.reset()
		sessionInvalidationRepository.clearIntentionalSignOut(sessionId)
	}
}
