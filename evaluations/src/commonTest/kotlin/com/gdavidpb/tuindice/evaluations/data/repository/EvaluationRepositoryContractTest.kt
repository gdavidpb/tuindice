package com.gdavidpb.tuindice.evaluations.data.repository

import com.gdavidpb.tuindice.base.domain.model.EvaluationScheduleMode
import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.evaluations.data.mapper.toLocalEvaluation
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationRemove
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationUpdate
import com.gdavidpb.tuindice.evaluations.testing.*
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class EvaluationRepositoryContractTest {
	@Test
	fun getEvaluationsFlow_refreshesLocalCache_whenCooldownIsDisabled() = runTest {
		val databaseDataSource = FakeDatabaseDataSource(initialEvaluations = emptyList())
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource(
			evaluations = listOf(DEFAULT_REMOTE_PENDING_EVALUATION)
		)
		val settingsDataSource = FakeSettingsDataSource(onCooldown = false)
		val repository = EvaluationDataRepository(
			databaseDataSource = databaseDataSource,
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = settingsDataSource
		)

		val evaluations = repository.getEvaluationsFlow().first()

		assertEquals(listOf(DEFAULT_PENDING_EVALUATION), evaluations)
		assertEquals(listOf(listOf(DEFAULT_LOCAL_PENDING_EVALUATION)), databaseDataSource.savedEvaluations)
		assertEquals(1, evaluationsApiDataSource.getEvaluationsCalls)
		assertTrue(settingsDataSource.cooldownMarked)
	}

	@Test
	fun addEvaluation_persistsEquivalentLocalAndRemoteRepresentations() = runTest {
		val databaseDataSource = FakeDatabaseDataSource(initialEvaluations = emptyList())
		val remoteCreatedEvaluation = DEFAULT_REMOTE_PENDING_EVALUATION.copy(id = "evaluation-3")
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource(
			evaluations = emptyList(),
			addResult = remoteCreatedEvaluation
		)
		val repository = EvaluationDataRepository(
			databaseDataSource = databaseDataSource,
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true)
		)
		val add = EvaluationAdd(
			reference = "evaluation-3",
			subjectId = DEFAULT_EVALUATION_SUBJECT.id,
			subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
			quarterId = DEFAULT_EVALUATION_SUBJECT.quarterId,
			type = EvaluationType.QUIZ,
			scheduleMode = EvaluationScheduleMode.DATED,
			date = DEFAULT_PENDING_EVALUATION.date,
			grade = null,
			maxGrade = 100.0
		)

		repository.addEvaluation(add)

		assertEquals(remoteCreatedEvaluation.toLocalEvaluation(), databaseDataSource.addedEvaluations.single())
		assertEquals("evaluation-3", evaluationsApiDataSource.addedEvaluations.single().id)
		assertEquals(DEFAULT_EVALUATION_SUBJECT.id, databaseDataSource.addedEvaluations.single().subjectId)
	}

	@Test
	fun addEvaluation_doesNotMutateLocalStateWhenRemoteFails() = runTest {
		val databaseDataSource = FakeDatabaseDataSource(initialEvaluations = emptyList())
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource(
			evaluations = emptyList(),
			addThrowable = IllegalStateException("boom")
		)
		val repository = EvaluationDataRepository(
			databaseDataSource = databaseDataSource,
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true)
		)

		val add = EvaluationAdd(
			reference = "evaluation-3",
			subjectId = DEFAULT_EVALUATION_SUBJECT.id,
			subjectCode = DEFAULT_EVALUATION_SUBJECT.code,
			quarterId = DEFAULT_EVALUATION_SUBJECT.quarterId,
			type = EvaluationType.QUIZ,
			scheduleMode = EvaluationScheduleMode.DATED,
			date = DEFAULT_PENDING_EVALUATION.date,
			grade = null,
			maxGrade = 100.0
		)

		assertFailsWith<IllegalStateException> {
			repository.addEvaluation(add)
		}

		assertEquals(emptyList(), databaseDataSource.addedEvaluations)
	}

	@Test
	fun updateEvaluation_persistsRemoteResponseLocally() = runTest {
		val databaseDataSource = FakeDatabaseDataSource()
		val remoteUpdatedEvaluation = DEFAULT_REMOTE_PENDING_EVALUATION.copy(grade = 4.0)
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource(
			updateResult = remoteUpdatedEvaluation
		)
		val repository = EvaluationDataRepository(
			databaseDataSource = databaseDataSource,
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true)
		)

		repository.updateEvaluation(
			EvaluationUpdate(
				id = DEFAULT_PENDING_EVALUATION.id,
				type = null,
				date = DEFAULT_PENDING_EVALUATION.date,
				grade = 4.0,
				maxGrade = null,
				scheduleMode = null
			)
		)

		assertEquals(remoteUpdatedEvaluation.toLocalEvaluation(), databaseDataSource.updatedEvaluations.single())
		assertEquals(4.0, evaluationsApiDataSource.updatedEvaluations.single().grade)
	}

	@Test
	fun removeEvaluation_doesNotMutateLocalStateWhenRemoteFails() = runTest {
		val databaseDataSource = FakeDatabaseDataSource()
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource(
			removeThrowable = IllegalStateException("boom")
		)
		val repository = EvaluationDataRepository(
			databaseDataSource = databaseDataSource,
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true)
		)

		assertFailsWith<IllegalStateException> {
			repository.removeEvaluation(
				EvaluationRemove(id = DEFAULT_PENDING_EVALUATION.id)
			)
		}

		assertEquals(emptyList(), databaseDataSource.removedEvaluationIds)
	}

	@Test
	fun updateEvaluation_removesLocalEntryWhenRemoteReturnsNotFound() = runTest {
		val databaseDataSource = FakeDatabaseDataSource()
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource(
			updateThrowable = clientRequestException(HttpStatusCode.NotFound, path = "/evaluations/v1/eid")
		)
		val repository = EvaluationDataRepository(
			databaseDataSource = databaseDataSource,
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true)
		)

		assertFailsWith<Exception> {
			repository.updateEvaluation(
				EvaluationUpdate(
					id = DEFAULT_PENDING_EVALUATION.id,
					type = null,
					date = DEFAULT_PENDING_EVALUATION.date,
					grade = 4.0,
					maxGrade = null,
					scheduleMode = null
				)
			)
		}

		assertEquals(listOf(DEFAULT_PENDING_EVALUATION.id), databaseDataSource.removedEvaluationIds)
	}

	@Test
	fun removeEvaluation_removesLocalEntryWhenRemoteReturnsNotFound() = runTest {
		val databaseDataSource = FakeDatabaseDataSource()
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource(
			removeThrowable = clientRequestException(HttpStatusCode.NotFound, path = "/evaluations/v1/eid")
		)
		val repository = EvaluationDataRepository(
			databaseDataSource = databaseDataSource,
			evaluationsApiDataSource = evaluationsApiDataSource,
			settingsDataSource = FakeSettingsDataSource(onCooldown = true)
		)

		assertFailsWith<Exception> {
			repository.removeEvaluation(
				EvaluationRemove(id = DEFAULT_PENDING_EVALUATION.id)
			)
		}

		assertEquals(listOf(DEFAULT_PENDING_EVALUATION.id), databaseDataSource.removedEvaluationIds)
	}

	@Test
	fun getAvailableSubjects_returnsFeatureLocalSubjectsAsDomainSubjects() = runTest {
		val repository = EvaluationDataRepository(
			databaseDataSource = FakeDatabaseDataSource(),
			evaluationsApiDataSource = FakeEvaluationsApiDataSource(),
			settingsDataSource = FakeSettingsDataSource(onCooldown = true)
		)

		val availableSubjects = repository.getAvailableSubjects()

		assertEquals(
			listOf<Subject>(DEFAULT_EVALUATION_SUBJECT, SECOND_EVALUATION_SUBJECT),
			availableSubjects
		)
	}
}
