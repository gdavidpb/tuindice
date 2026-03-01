package com.gdavidpb.tuindice.evaluations.data.repository

import com.gdavidpb.tuindice.base.domain.model.EvaluationType
import com.gdavidpb.tuindice.base.domain.model.subject.Subject
import com.gdavidpb.tuindice.evaluations.domain.model.EvaluationAdd
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_EVALUATION_SUBJECT
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_LOCAL_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.DEFAULT_REMOTE_PENDING_EVALUATION
import com.gdavidpb.tuindice.evaluations.testing.FakeDatabaseDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeEvaluationsApiDataSource
import com.gdavidpb.tuindice.evaluations.testing.FakeSettingsDataSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
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
		val evaluationsApiDataSource = FakeEvaluationsApiDataSource(evaluations = emptyList())
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
			date = DEFAULT_PENDING_EVALUATION.date,
			grade = null,
			maxGrade = 100.0
		)

		repository.addEvaluation(add)

		assertEquals("evaluation-3", databaseDataSource.addedEvaluations.single().id)
		assertEquals("evaluation-3", evaluationsApiDataSource.addedEvaluations.single().id)
		assertEquals(DEFAULT_EVALUATION_SUBJECT.id, databaseDataSource.addedEvaluations.single().subjectId)
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
			listOf<Subject>(DEFAULT_EVALUATION_SUBJECT, com.gdavidpb.tuindice.evaluations.testing.SECOND_EVALUATION_SUBJECT),
			availableSubjects
		)
	}
}
