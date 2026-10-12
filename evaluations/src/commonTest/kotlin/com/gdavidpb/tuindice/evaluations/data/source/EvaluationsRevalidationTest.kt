@file:OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)

package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.model.RemoteEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.data.repository.EvaluationsApiDataRepository
import com.gdavidpb.tuindice.evaluations.data.repository.SettingsDataRepository
import com.gdavidpb.tuindice.evaluations.testing.FakeDatabaseDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeEvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeIdentifierRepository
import com.gdavidpb.tuindice.evaluations.testing.FakeSettingsDataSource
import com.gdavidpb.tuindice.evaluations.testing.createEvaluationsMutationEngine
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// What the sync hands off after a success (EvaluationsRevalidationRepository) and how it shares
// the download with the tab (EvaluationRepository.updateEvaluations).
class EvaluationsRevalidationTest {
	@Test
	fun revalidateEvaluations_downloadsThem_whenTheyWereNeverDownloaded_evenOnCooldown() = runTest {
		val api = FakeEvaluationsApiDataSource()
		val database = FakeDatabaseDataSource(initialSnapshot = neverDownloaded())
		val settings = FakeSettingsDataSource(onCooldown = true)

		repository(api = api, database = database, settings = settings).revalidateEvaluations()

		assertEquals(1, api.getEvaluationsCalls)
		assertEquals(1, database.savedSnapshots.size)
		assertTrue(settings.cooldownMarked)
	}

	@Test
	fun revalidateEvaluations_downloadsThem_whenThereIsNoCooldown() = runTest {
		val api = FakeEvaluationsApiDataSource()

		repository(api = api, settings = FakeSettingsDataSource(onCooldown = false)).revalidateEvaluations()

		assertEquals(1, api.getEvaluationsCalls)
	}

	@Test
	fun revalidateEvaluations_doesNotTouchTheNetwork_whenCooldownHoldsAndTheyWereDownloaded() = runTest {
		val api = FakeEvaluationsApiDataSource()
		val database = FakeDatabaseDataSource()
		val settings = FakeSettingsDataSource(onCooldown = true)

		repository(api = api, database = database, settings = settings).revalidateEvaluations()

		assertEquals(0, api.getEvaluationsCalls)
		assertTrue(database.savedSnapshots.isEmpty())
		assertEquals(false, settings.cooldownMarked)
	}

	@Test
	fun twoSimultaneousCalls_downloadOnlyOnce() = runTest {
		val gate = CompletableDeferred<Unit>()
		val api = GatedEvaluationsApi(gate = gate)
		val settings = FlippingCooldownSettings()
		val repository = repository(
			api = api,
			database = FakeDatabaseDataSource(initialSnapshot = neverDownloaded()),
			settings = settings
		)

		// The tab opens while the revalidation after a sync is still downloading.
		val revalidation = launch { repository.revalidateEvaluations() }
		val tab = launch { repository.updateEvaluations() }
		runCurrent()
		assertEquals(1, api.getEvaluationsCalls, "the second caller waits for the first download")

		gate.complete(Unit)
		runCurrent()

		assertTrue(revalidation.isCompleted && tab.isCompleted)
		assertEquals(1, api.getEvaluationsCalls, "the second caller finds what the first one saved")
	}

	@Test
	fun revalidateEvaluations_waitsAfterAFailedDownload_butTheTabKeepsTrying() = runTest {
		val api = FakeEvaluationsApiDataSource(getEvaluationsThrowable = IllegalStateException("offline"))
		val repository = repository(
			api = api,
			database = FakeDatabaseDataSource(initialSnapshot = neverDownloaded()),
			settings = FakeSettingsDataSource(onCooldown = false)
		)

		assertFailsWith<IllegalStateException> { repository.revalidateEvaluations() }
		assertEquals(1, api.getEvaluationsCalls)

		// The next automatic revalidation is held off: no request, no error.
		repository.revalidateEvaluations()
		assertEquals(1, api.getEvaluationsCalls)

		// The student opening the tab is never held off.
		assertFailsWith<IllegalStateException> { repository.updateEvaluations(forceRemote = false) }
		assertEquals(2, api.getEvaluationsCalls)
	}

	@Test
	fun revalidateEvaluations_triesAgain_afterTheSessionMemoryIsCleared() = runTest {
		val api = FakeEvaluationsApiDataSource(getEvaluationsThrowable = IllegalStateException("offline"))
		val repository = repository(
			api = api,
			database = FakeDatabaseDataSource(initialSnapshot = neverDownloaded()),
			settings = FakeSettingsDataSource(onCooldown = false)
		)

		assertFailsWith<IllegalStateException> { repository.revalidateEvaluations() }
		repository.clearSessionMemory()
		assertFailsWith<IllegalStateException> { repository.revalidateEvaluations() }

		assertEquals(2, api.getEvaluationsCalls)
	}

	@Test
	fun aSuccessfulDownload_liftsTheHold() = runTest {
		val api = FlakyEvaluationsApi()
		val repository = repository(
			api = api,
			database = FakeDatabaseDataSource(initialSnapshot = neverDownloaded()),
			settings = FakeSettingsDataSource(onCooldown = false)
		)

		assertFailsWith<IllegalStateException> { repository.revalidateEvaluations() }
		api.failing = false
		repository.updateEvaluations(forceRemote = true)
		repository.revalidateEvaluations()

		assertEquals(3, api.calls, "the revalidation after a good download is not held off")
	}

	private fun TestScope.repository(
		api: EvaluationsApiDataRepository = FakeEvaluationsApiDataSource(),
		database: FakeDatabaseDataSource = FakeDatabaseDataSource(),
		settings: SettingsDataRepository = FakeSettingsDataSource(onCooldown = false)
	) = EvaluationDataSource(
		databaseDataSource = database,
		evaluationsApiDataSource = api,
		settingsDataSource = settings,
		mutationEngine = createEvaluationsMutationEngine(),
		identifierRepository = FakeIdentifierRepository(),
		clock = Clock.System
	)

	private fun neverDownloaded() = LocalEvaluationsSnapshot(hasSynced = false, evaluations = emptyList())
}

// Holds its download until the gate opens, so two callers can overlap.
private class GatedEvaluationsApi(
	private val gate: CompletableDeferred<Unit>,
	private val delegate: EvaluationsApiDataRepository = FakeEvaluationsApiDataSource()
) : EvaluationsApiDataRepository by delegate {
	var getEvaluationsCalls = 0

	override suspend fun getEvaluations(): RemoteEvaluationsSnapshot {
		getEvaluationsCalls++
		gate.await()
		return delegate.getEvaluations()
	}
}

private class FlakyEvaluationsApi(
	private val delegate: EvaluationsApiDataRepository = FakeEvaluationsApiDataSource()
) : EvaluationsApiDataRepository by delegate {
	var failing = true
	var calls = 0

	override suspend fun getEvaluations(): RemoteEvaluationsSnapshot {
		calls++
		if (failing) error("offline")
		return delegate.getEvaluations()
	}
}

// Like the real settings: once a download marks the cooldown, it reads as on cooldown.
private class FlippingCooldownSettings : SettingsDataRepository {
	private var onCooldown = false

	override suspend fun isGetEvaluationsOnCooldown(): Boolean = onCooldown

	override suspend fun setGetEvaluationsOnCooldown() {
		onCooldown = true
	}
}
