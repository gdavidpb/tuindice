package com.gdavidpb.tuindice.debug

import com.gdavidpb.tuindice.base.domain.model.MainSection
import com.gdavidpb.tuindice.base.domain.model.SessionSnapshot
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.testkit.base.repository.FakeCredentialsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSessionRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSettingsRepository
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.wizard.presentation.model.contextualCoachmarks
import com.gdavidpb.tuindice.wizard.presentation.model.persistedId
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DebugSessionSeederTest {
	private val session = FakeSessionRepository()
	private val settings = FakeSettingsRepository()
	private val credentials = FakeCredentialsRepository()
	private val syncStatus = FakeSyncStatusRepository()
	private val seeder = DebugSessionSeeder(
		sessionRepository = session,
		settingsRepository = settings,
		credentialsRepository = credentials,
		syncStatusRepository = syncStatus
	)

	@Test
	fun seed_storesTheSnapshotThePasswordAndTheDataOwner() = runTest {
		val seed = DebugSessionSeed(
			sessionId = "session-77",
			accessToken = "access-77",
			refreshToken = "refresh-77",
			usbId = "77-77777",
			password = "secret-77",
			coachmarksSeen = true,
			mainSection = MainSection.PENSUM
		)

		seeder.seed(seed)

		assertEquals(
			SessionSnapshot(
				sessionId = "session-77",
				accessToken = "access-77",
				refreshToken = "refresh-77",
				usbId = "77-77777"
			),
			session.getActiveSessionSnapshot()
		)
		assertEquals("secret-77", credentials.getPassword())
		assertEquals("77-77777", settings.getLocalDataOwner())
		assertEquals(MainSection.PENSUM, settings.getLastMainSection())
	}

	@Test
	fun seed_marksEveryContextualCoachmarkWhenSeen() = runTest {
		seeder.seed(DebugSessionSeed.Canonical.copy(coachmarksSeen = true))

		assertEquals(
			contextualCoachmarks().map { it.id.persistedId }.toSet(),
			settings.getSeenCoachmarkIds()
		)
	}

	@Test
	fun seed_leavesCoachmarksPendingWhenAsked() = runTest {
		seeder.seed(DebugSessionSeed.Canonical.copy(coachmarksSeen = false))

		assertTrue(settings.getSeenCoachmarkIds().isEmpty())
	}

	@Test
	fun seed_clearsThePreviousSessionFirst() = runTest {
		settings.markCoachmarkSeen("stale-coachmark")
		credentials.setPassword("stale-password")
		syncStatus.emitSyncStatus(SyncStatus.Failed)

		seeder.seed(DebugSessionSeed.Canonical.copy(coachmarksSeen = false))

		assertTrue(session.cleared)
		assertTrue(settings.cleared)
		assertEquals(1, credentials.clearCalls)
		assertEquals(1, syncStatus.resetCalls)
		assertTrue(settings.getSeenCoachmarkIds().isEmpty())
		assertEquals(SyncStatus.Healthy, syncStatus.getSyncStatus())
		assertEquals(listOf(DebugSessionSeed.Canonical.password), listOf(credentials.getPassword()))
	}
}
