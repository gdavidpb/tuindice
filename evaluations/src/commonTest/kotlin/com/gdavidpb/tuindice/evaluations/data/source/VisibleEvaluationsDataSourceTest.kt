@file:OptIn(ExperimentalTime::class)

package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.base.domain.model.ObservedSyncedSnapshot
import com.gdavidpb.tuindice.evaluations.data.model.LocalEvaluationsSnapshot
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_LOCAL_COMPLETED_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_LOCAL_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.FakeDatabaseDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeEvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeIdentifierRepository
import com.gdavidpb.tuindice.evaluations.testing.FakeSettingsDataSource
import com.gdavidpb.tuindice.evaluations.testing.RecordingEvaluationRepository
import com.gdavidpb.tuindice.evaluations.testing.createEvaluationsMutationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class VisibleEvaluationsDataSourceTest {
	@Test
	fun observeVisibleEvaluationsFlow_deliversWhatTheTabObserves_withSyncedFlag() = runTest {
		val tabRepository = RecordingEvaluationRepository(
			initialEvaluations = listOf(DEFAULT_PENDING_EVALUATION, DEFAULT_COMPLETED_EVALUATION),
			hasSyncedEvaluationsFlow = kotlinx.coroutines.flow.flowOf(true)
		)
		val source = VisibleEvaluationsDataSource(evaluationRepository = tabRepository)

		val visible = source.observeVisibleEvaluationsFlow().first()

		assertEquals(tabRepository.observeEvaluationsSnapshotFlow().first(), visible)
		assertEquals(
			ObservedSyncedSnapshot(
				value = listOf(DEFAULT_PENDING_EVALUATION, DEFAULT_COMPLETED_EVALUATION),
				hasSynced = true
			),
			visible
		)
	}

	@Test
	fun observeVisibleEvaluationsFlow_tellsNotDownloadedYetApartFromNone() = runTest {
		val notDownloaded = VisibleEvaluationsDataSource(
			RecordingEvaluationRepository(
				initialEvaluations = emptyList(),
				hasSyncedEvaluationsFlow = kotlinx.coroutines.flow.flowOf(false)
			)
		)
		val downloadedAndEmpty = VisibleEvaluationsDataSource(
			RecordingEvaluationRepository(
				initialEvaluations = emptyList(),
				hasSyncedEvaluationsFlow = kotlinx.coroutines.flow.flowOf(true)
			)
		)

		assertEquals(
			ObservedSyncedSnapshot(value = emptyList(), hasSynced = false),
			notDownloaded.observeVisibleEvaluationsFlow().first()
		)
		assertEquals(
			ObservedSyncedSnapshot(value = emptyList(), hasSynced = true),
			downloadedAndEmpty.observeVisibleEvaluationsFlow().first()
		)
	}

	@Test
	fun observeVisibleEvaluationsFlow_followsTheSnapshotOfTheRealRepository_confirmedAndPendingAlike() = runTest {
		// The database layer hands the repository the confirmed rows already merged with the pending
		// ones of the outbox; the contract must show that very list, in the same order.
		val database = FakeDatabaseDataSource(
			initialSnapshot = LocalEvaluationsSnapshot(
				hasSynced = true,
				evaluations = listOf(DEFAULT_LOCAL_PENDING_EVALUATION, DEFAULT_LOCAL_COMPLETED_EVALUATION)
			)
		)
		val repository = EvaluationDataSource(
			databaseDataSource = database,
			evaluationsApiDataSource = FakeEvaluationsApiDataSource(),
			settingsDataSource = FakeSettingsDataSource(onCooldown = true),
			mutationEngine = createEvaluationsMutationEngine(),
			identifierRepository = FakeIdentifierRepository(),
			clock = Clock.System
		)
		val source = VisibleEvaluationsDataSource(evaluationRepository = repository)

		val visible = source.observeVisibleEvaluationsFlow().first()

		assertEquals(repository.getEvaluationsSnapshot(), visible)
		assertEquals(
			listOf(DEFAULT_LOCAL_PENDING_EVALUATION.id, DEFAULT_LOCAL_COMPLETED_EVALUATION.id),
			visible.value.map { evaluation -> evaluation.id }
		)
	}

	@Test
	fun observeVisibleEvaluationsFlow_emitsAgain_whenTheEvaluationsChange() = runTest {
		val evaluations = MutableStateFlow(listOf(DEFAULT_PENDING_EVALUATION))
		val source = VisibleEvaluationsDataSource(
			RecordingEvaluationRepository(evaluationsFlow = evaluations)
		)
		val emitted = mutableListOf<List<String>>()

		backgroundScope.launch {
			source.observeVisibleEvaluationsFlow().collect { snapshot ->
				emitted += snapshot.value.map { evaluation -> evaluation.id }
			}
		}
		runCurrent()
		evaluations.value = listOf(DEFAULT_PENDING_EVALUATION, DEFAULT_COMPLETED_EVALUATION)
		runCurrent()

		assertEquals(
			listOf(
				listOf(DEFAULT_PENDING_EVALUATION.id),
				listOf(DEFAULT_PENDING_EVALUATION.id, DEFAULT_COMPLETED_EVALUATION.id)
			),
			emitted
		)
	}
}
