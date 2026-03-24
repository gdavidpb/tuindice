package com.gdavidpb.tuindice.record.data.repository

import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.repository.IdentifierRepository
import com.gdavidpb.tuindice.base.domain.repository.MutationOutboxRepository
import com.gdavidpb.tuindice.record.domain.model.QuarterRemove
import com.gdavidpb.tuindice.record.domain.model.SubjectGradeSet
import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutation
import com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSetSubjectGradeAck
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_LOCAL_QUARTER
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_LOCAL_SUBJECT
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_QUARTER
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_REMOTE_SUBJECT
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_REMOTE_QUARTER
import com.gdavidpb.tuindice.record.testing.FakeMutationOutboxRepository
import com.gdavidpb.tuindice.record.testing.FakeQuarterLocalDataSource
import com.gdavidpb.tuindice.record.testing.FakeQuarterRemoteDataSource
import com.gdavidpb.tuindice.record.testing.FakeQuarterSettingsDataSource
import com.gdavidpb.tuindice.record.testing.SetSubjectGradeCall
import com.gdavidpb.tuindice.testkit.base.repository.FakeIdentifierRepository
import com.gdavidpb.tuindice.testkit.ktor.clientRequestException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class QuarterRepositoryContractTest {
	private val deletableQuarter = DEFAULT_RECORD_QUARTER.copy(
		isCurrent = false,
		isReadOnly = false
	)
	private val deletableLocalQuarter = DEFAULT_RECORD_LOCAL_QUARTER.copy(
		isCurrent = false,
		isReadOnly = false
	)

	@Test
	fun observeQuartersFlow_emitsLocalQuarters_withoutRefreshing() = runTest {
		val localDataSource = FakeQuarterLocalDataSource(
			initialQuarters = listOf(DEFAULT_RECORD_LOCAL_QUARTER)
		)
		val remoteDataSource = FakeQuarterRemoteDataSource(
			quarters = listOf(DEFAULT_RECORD_REMOTE_QUARTER)
		)
		val repository = repository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeQuarterSettingsDataSource(onCooldown = false)
		)

		val quarters = repository.observeQuartersFlow().first()

		assertEquals(listOf(DEFAULT_RECORD_QUARTER), quarters)
		assertEquals(0, remoteDataSource.getQuartersCalls)
		assertTrue(localDataSource.savedQuarters.isEmpty())
	}

	@Test
	fun updateQuarters_refreshesLocalCache_whenCooldownIsDisabled() = runTest {
		val localDataSource = FakeQuarterLocalDataSource(initialQuarters = emptyList())
		val remoteDataSource = FakeQuarterRemoteDataSource(
			quarters = listOf(DEFAULT_RECORD_REMOTE_QUARTER)
		)
		val settingsDataSource = FakeQuarterSettingsDataSource(onCooldown = false)
		val repository = repository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = settingsDataSource
		)

		repository.updateQuarters()

		assertEquals(listOf(listOf(DEFAULT_RECORD_LOCAL_QUARTER)), localDataSource.savedQuarters)
		assertEquals(1, remoteDataSource.getQuartersCalls)
		assertTrue(settingsDataSource.cooldownMarked)
	}

	@Test
	fun setSubjectGrade_whenPreviewOnly_updatesLocalPreviewWithoutCallingRemoteOrOutbox() = runTest {
		val localDataSource = FakeQuarterLocalDataSource()
		val remoteDataSource = FakeQuarterRemoteDataSource()
		val outboxRepository = FakeMutationOutboxRepository<RecordMutation>()
		val repository = repository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			outboxRepository = outboxRepository
		)

		repository.setSubjectGrade(
			SubjectGradeSet(
				quarterId = DEFAULT_RECORD_QUARTER.id,
				id = DEFAULT_RECORD_QUARTER.subjects.single().id,
				grade = 85,
				commit = false
			)
		)

		assertEquals(
			SetSubjectGradeCall(
				quarterId = DEFAULT_RECORD_QUARTER.id,
				subjectId = DEFAULT_RECORD_QUARTER.subjects.single().id,
				grade = 85,
				commit = false
			),
			localDataSource.lastSetSubjectGradeArgs
		)
		assertTrue(remoteDataSource.setSubjectGradeCalls.isEmpty())
		assertTrue(outboxRepository.getPendingMutations().isEmpty())
	}

	@Test
	fun setSubjectGrade_whenCommitted_enqueuesAndSendsMutationImmediately() = runTest {
		val localDataSource = FakeQuarterLocalDataSource()
		val remoteDataSource = FakeQuarterRemoteDataSource()
		val outboxRepository = FakeMutationOutboxRepository<RecordMutation>()
		val repository = repository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			outboxRepository = outboxRepository
		)

		repository.setSubjectGrade(
			SubjectGradeSet(
				quarterId = DEFAULT_RECORD_QUARTER.id,
				id = DEFAULT_RECORD_QUARTER.subjects.single().id,
				grade = 85,
				commit = true
			)
		)

		assertEquals(
			SetSubjectGradeCall(
				quarterId = DEFAULT_RECORD_QUARTER.id,
				subjectId = DEFAULT_RECORD_QUARTER.subjects.single().id,
				grade = 85,
				commit = true
			),
			localDataSource.lastSetSubjectGradeArgs
		)
		assertEquals(1, remoteDataSource.setSubjectGradeCalls.size)
		assertEquals("mutation-1", remoteDataSource.setSubjectGradeCalls.single().mutationId)
		assertEquals(DEFAULT_RECORD_LOCAL_SUBJECT.revision, remoteDataSource.setSubjectGradeCalls.single().expectedRevision)
		assertTrue(outboxRepository.getPendingMutations().isEmpty())
		assertEquals(85, localDataSource.getQuarter(DEFAULT_RECORD_QUARTER.id)?.subjects?.single()?.grade)
	}

	@Test
	fun setSubjectGrade_whenRemoteFails_keepsPendingMutationAndRethrows() = runTest {
		val localDataSource = FakeQuarterLocalDataSource()
		val remoteDataSource = FakeQuarterRemoteDataSource(
			setSubjectGradeThrowable = IllegalStateException("boom")
		)
		val outboxRepository = FakeMutationOutboxRepository<RecordMutation>()
		val repository = repository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			outboxRepository = outboxRepository
		)

		assertFailsWith<IllegalStateException> {
			repository.setSubjectGrade(
				SubjectGradeSet(
					quarterId = DEFAULT_RECORD_QUARTER.id,
					id = DEFAULT_RECORD_QUARTER.subjects.single().id,
					grade = 85,
					commit = true
				)
			)
		}

		val pending = outboxRepository.getPendingMutations().single()
		assertEquals(PendingMutationStatus.Failed, pending.status)
		assertEquals(85, localDataSource.getQuarter(DEFAULT_RECORD_QUARTER.id)?.subjects?.single()?.grade)
	}

	@Test
	fun setSubjectGrade_whenNewerMutationStartsBeforeOlderAck_ignoresOlderAckEvenBeforeOutboxReplacement() = runTest {
		val firstCallStarted = CompletableDeferred<Unit>()
		val releaseFirstAck = CompletableDeferred<Unit>()
		val secondReplaceStarted = CompletableDeferred<Unit>()
		val releaseSecondReplace = CompletableDeferred<Unit>()
		val secondCallStarted = CompletableDeferred<Unit>()
		val releaseSecondAck = CompletableDeferred<Unit>()
		val localDataSource = FakeQuarterLocalDataSource()
		val outboxRepository = object : MutationOutboxRepository<RecordMutation> {
			private val state = MutableStateFlow<List<PendingMutation<RecordMutation>>>(emptyList())
			private var replaceCalls = 0

			override fun observePendingMutations(): Flow<List<PendingMutation<RecordMutation>>> = state

			override suspend fun getPendingMutations(): List<PendingMutation<RecordMutation>> = state.value

			override suspend fun getPendingMutation(mutationId: String): PendingMutation<RecordMutation>? {
				return state.value.firstOrNull { mutation -> mutation.mutationId == mutationId }
			}

			override suspend fun replacePendingMutation(mutation: PendingMutation<RecordMutation>) {
				replaceCalls += 1
				if (replaceCalls == 2) {
					secondReplaceStarted.complete(Unit)
					releaseSecondReplace.await()
				}

				state.value = state.value
					.filterNot { pending -> pending.mutation.replaceKey == mutation.mutation.replaceKey }
					.plus(mutation)
			}

			override suspend fun savePendingMutation(mutation: PendingMutation<RecordMutation>) {
				state.value = state.value
					.filterNot { pending -> pending.mutationId == mutation.mutationId }
					.plus(mutation)
			}

			override suspend fun deletePendingMutation(mutationId: String) {
				state.value = state.value.filterNot { mutation -> mutation.mutationId == mutationId }
			}
		}
		val remoteDataSource = object : QuarterRemoteDataSource {
			private var setCalls = 0

			override suspend fun getQuarters() = listOf(DEFAULT_RECORD_REMOTE_QUARTER)

			override suspend fun getQuarter(qid: String) = DEFAULT_RECORD_REMOTE_QUARTER

			override suspend fun removeQuarter(
				qid: String,
				mutationId: String,
				expectedRevision: Long
			) = error("unused")

			override suspend fun addQuarter(
				quarter: com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter,
				mutationId: String,
				expectedRevision: Long
			) = error("unused")

			override suspend fun setSubjectGrade(
				qid: String,
				sid: String,
				grade: Int,
				mutationId: String,
				expectedRevision: Long
			): RemoteSetSubjectGradeAck {
				setCalls += 1
				val ack = RemoteSetSubjectGradeAck(
					mutationId = mutationId,
					subject = DEFAULT_RECORD_REMOTE_SUBJECT.copy(grade = grade),
					affectedQuarters = listOf(
						DEFAULT_RECORD_REMOTE_QUARTER.copy(
							grade = grade.toDouble(),
							gradeSum = grade.toDouble(),
							subjects = listOf(DEFAULT_RECORD_REMOTE_SUBJECT.copy(grade = grade))
						)
					)
				)

				when (setCalls) {
					1 -> {
						firstCallStarted.complete(Unit)
						releaseFirstAck.await()
					}

					2 -> {
						secondCallStarted.complete(Unit)
						releaseSecondAck.await()
					}
				}

				return ack
			}
		}
		val identifierRepository = object : IdentifierRepository {
			private var nextId = 0

			override fun generateRandomIdentifier(): String {
				nextId += 1
				return "mutation-$nextId"
			}
		}
		val repository = QuarterDataRepository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeQuarterSettingsDataSource(onCooldown = true),
			mutationOutboxRepository = outboxRepository,
			identifierRepository = identifierRepository
		)

		val firstMutationJob = launch {
			repository.setSubjectGrade(
				SubjectGradeSet(
					quarterId = DEFAULT_RECORD_QUARTER.id,
					id = DEFAULT_RECORD_QUARTER.subjects.single().id,
					grade = 80,
					commit = true
				)
			)
		}

		firstCallStarted.await()

		val secondMutationJob = launch {
			repository.setSubjectGrade(
				SubjectGradeSet(
					quarterId = DEFAULT_RECORD_QUARTER.id,
					id = DEFAULT_RECORD_QUARTER.subjects.single().id,
					grade = 90,
					commit = true
				)
			)
		}

		secondReplaceStarted.await()
		releaseFirstAck.complete(Unit)

		assertEquals(90, localDataSource.getQuarter(DEFAULT_RECORD_QUARTER.id)?.subjects?.single()?.grade)

		releaseSecondReplace.complete(Unit)
		secondCallStarted.await()
		releaseSecondAck.complete(Unit)
		firstMutationJob.join()
		secondMutationJob.join()

		assertEquals(90, localDataSource.getQuarter(DEFAULT_RECORD_QUARTER.id)?.subjects?.single()?.grade)
	}

	@Test
	fun setSubjectGrade_whenLaterAckCarriesOlderRevisionForDifferentSubject_keepsNewestConfirmedGrades() = runTest {
		val localSubjectA = DEFAULT_RECORD_LOCAL_SUBJECT.copy(
			id = "subject-a",
			quarterId = DEFAULT_RECORD_LOCAL_QUARTER.id,
			code = "MAT101",
			name = "Materia A",
			credits = 4,
			grade = 3,
			revision = 1L
		)
		val localSubjectB = DEFAULT_RECORD_LOCAL_SUBJECT.copy(
			id = "subject-b",
			quarterId = DEFAULT_RECORD_LOCAL_QUARTER.id,
			code = "MAT102",
			name = "Materia B",
			credits = 4,
			grade = 1,
			revision = 1L
		)
		val localQuarter = DEFAULT_RECORD_LOCAL_QUARTER.copy(
			grade = 2.0,
			gradeSum = 2.0,
			credits = 8,
			creditsSum = 8,
			subjects = listOf(localSubjectA, localSubjectB)
		)
		val remoteSubjectA = DEFAULT_RECORD_REMOTE_SUBJECT.copy(
			id = localSubjectA.id,
			quarterId = localSubjectA.quarterId,
			code = localSubjectA.code,
			name = localSubjectA.name,
			credits = localSubjectA.credits,
			grade = localSubjectA.grade,
			revision = localSubjectA.revision
		)
		val remoteSubjectB = DEFAULT_RECORD_REMOTE_SUBJECT.copy(
			id = localSubjectB.id,
			quarterId = localSubjectB.quarterId,
			code = localSubjectB.code,
			name = localSubjectB.name,
			credits = localSubjectB.credits,
			grade = localSubjectB.grade,
			revision = localSubjectB.revision
		)
		val initialRemoteQuarter = DEFAULT_RECORD_REMOTE_QUARTER.copy(
			grade = localQuarter.grade,
			gradeSum = localQuarter.gradeSum,
			credits = localQuarter.credits,
			creditsSum = localQuarter.creditsSum,
			subjects = listOf(remoteSubjectA, remoteSubjectB)
		)
		val localDataSource = FakeQuarterLocalDataSource(
			initialQuarters = listOf(localQuarter)
		)
		val remoteDataSource = object : QuarterRemoteDataSource {
			private var setCalls = 0

			override suspend fun getQuarters() = listOf(initialRemoteQuarter)

			override suspend fun getQuarter(qid: String) = initialRemoteQuarter

			override suspend fun removeQuarter(
				qid: String,
				mutationId: String,
				expectedRevision: Long
			) = error("unused")

			override suspend fun addQuarter(
				quarter: com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter,
				mutationId: String,
				expectedRevision: Long
			) = error("unused")

			override suspend fun setSubjectGrade(
				qid: String,
				sid: String,
				grade: Int,
				mutationId: String,
				expectedRevision: Long
			): RemoteSetSubjectGradeAck {
				setCalls += 1

				return when (setCalls) {
					1 -> RemoteSetSubjectGradeAck(
						mutationId = mutationId,
						subject = remoteSubjectA.copy(grade = 5, revision = 2L),
						affectedQuarters = listOf(
							initialRemoteQuarter.copy(
								grade = 3.0,
								gradeSum = 3.0,
								subjects = listOf(
									remoteSubjectA.copy(grade = 5, revision = 2L),
									remoteSubjectB
								)
							)
						)
					)

					2 -> RemoteSetSubjectGradeAck(
						mutationId = mutationId,
						subject = remoteSubjectB.copy(grade = 5, revision = 2L),
						affectedQuarters = listOf(
							initialRemoteQuarter.copy(
								grade = 4.0,
								gradeSum = 4.0,
								subjects = listOf(
									remoteSubjectA,
									remoteSubjectB.copy(grade = 5, revision = 2L)
								)
							)
						)
					)

					else -> error("unexpected call")
				}
			}
		}
		val identifierRepository = object : IdentifierRepository {
			private var nextId = 0

			override fun generateRandomIdentifier(): String {
				nextId += 1
				return "mutation-$nextId"
			}
		}
		val outboxRepository = FakeMutationOutboxRepository<RecordMutation>()
		val repository = QuarterDataRepository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeQuarterSettingsDataSource(onCooldown = true),
			mutationOutboxRepository = outboxRepository,
			identifierRepository = identifierRepository
		)

		repository.setSubjectGrade(
			SubjectGradeSet(
				quarterId = localQuarter.id,
				id = localSubjectA.id,
				grade = 5,
				commit = true
			)
		)
		repository.setSubjectGrade(
			SubjectGradeSet(
				quarterId = localQuarter.id,
				id = localSubjectB.id,
				grade = 5,
				commit = true
			)
		)

		val updatedQuarter = localDataSource.getQuarter(localQuarter.id)
		assertEquals(5, updatedQuarter?.subjects?.first { subject -> subject.id == localSubjectA.id }?.grade)
		assertEquals(2L, updatedQuarter?.subjects?.first { subject -> subject.id == localSubjectA.id }?.revision)
		assertEquals(5, updatedQuarter?.subjects?.first { subject -> subject.id == localSubjectB.id }?.grade)
		assertEquals(2L, updatedQuarter?.subjects?.first { subject -> subject.id == localSubjectB.id }?.revision)
		assertTrue(outboxRepository.getPendingMutations().isEmpty())
	}

	@Test
	fun setSubjectGrade_whenPreconditionFailsForSupersededSameSubject_rebasesAndRetriesLatestMutation() = runTest {
		val firstCallStarted = CompletableDeferred<Unit>()
		val releaseFirstAck = CompletableDeferred<Unit>()
		var remoteSubject = DEFAULT_RECORD_REMOTE_SUBJECT
		val remoteCalls = mutableListOf<Pair<Int, Long>>()
		val localDataSource = FakeQuarterLocalDataSource()
		val outboxRepository = FakeMutationOutboxRepository<RecordMutation>()
		val remoteDataSource = object : QuarterRemoteDataSource {
			override suspend fun getQuarters() = listOf(
				DEFAULT_RECORD_REMOTE_QUARTER.copy(
					grade = remoteSubject.grade.toDouble(),
					gradeSum = remoteSubject.grade.toDouble(),
					subjects = listOf(remoteSubject)
				)
			)

			override suspend fun getQuarter(qid: String) = getQuarters().single()

			override suspend fun removeQuarter(
				qid: String,
				mutationId: String,
				expectedRevision: Long
			) = error("unused")

			override suspend fun addQuarter(
				quarter: com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter,
				mutationId: String,
				expectedRevision: Long
			) = error("unused")

			override suspend fun setSubjectGrade(
				qid: String,
				sid: String,
				grade: Int,
				mutationId: String,
				expectedRevision: Long
			): RemoteSetSubjectGradeAck {
				remoteCalls += grade to expectedRevision

				return when (remoteCalls.size) {
					1 -> {
						firstCallStarted.complete(Unit)
						releaseFirstAck.await()
						remoteSubject = remoteSubject.copy(
							grade = grade,
							revision = expectedRevision + 1
						)
						RemoteSetSubjectGradeAck(
							mutationId = mutationId,
							subject = remoteSubject,
							affectedQuarters = getQuarters()
						)
					}

					2 ->
						throw clientRequestException(
							statusCode = HttpStatusCode.PreconditionFailed,
							path = "/quarters/v1/$qid/subjects/$sid"
						)

					3 -> {
						remoteSubject = remoteSubject.copy(
							grade = grade,
							revision = expectedRevision + 1
						)
						RemoteSetSubjectGradeAck(
							mutationId = mutationId,
							subject = remoteSubject,
							affectedQuarters = getQuarters()
						)
					}

					else -> error("unexpected call")
				}
			}
		}
		val identifierRepository = object : IdentifierRepository {
			private var nextId = 0

			override fun generateRandomIdentifier(): String {
				nextId += 1
				return "mutation-$nextId"
			}
		}
		val repository = QuarterDataRepository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeQuarterSettingsDataSource(onCooldown = true),
			mutationOutboxRepository = outboxRepository,
			identifierRepository = identifierRepository
		)

		val firstMutationJob = launch {
			repository.setSubjectGrade(
				SubjectGradeSet(
					quarterId = DEFAULT_RECORD_QUARTER.id,
					id = DEFAULT_RECORD_QUARTER.subjects.single().id,
					grade = 80,
					commit = true
				)
			)
		}

		firstCallStarted.await()

		val secondMutationJob = launch {
			repository.setSubjectGrade(
				SubjectGradeSet(
					quarterId = DEFAULT_RECORD_QUARTER.id,
					id = DEFAULT_RECORD_QUARTER.subjects.single().id,
					grade = 90,
					commit = true
				)
			)
		}

		releaseFirstAck.complete(Unit)
		firstMutationJob.join()
		secondMutationJob.join()

		assertEquals(listOf(80 to 1L, 90 to 1L, 90 to 2L), remoteCalls)
		assertEquals(90, localDataSource.getQuarter(DEFAULT_RECORD_QUARTER.id)?.subjects?.single()?.grade)
		assertEquals(3L, localDataSource.getQuarter(DEFAULT_RECORD_QUARTER.id)?.subjects?.single()?.revision)
		assertTrue(outboxRepository.getPendingMutations().isEmpty())
	}

	@Test
	fun updateQuarters_doesNotOverwriteCommittedGrade_whenEarlierRefreshReturnsStaleSnapshot() = runTest {
		val refreshStarted = CompletableDeferred<Unit>()
		val releaseStaleRefresh = CompletableDeferred<Unit>()
		val localDataSource = FakeQuarterLocalDataSource()
		val staleRemoteQuarter = DEFAULT_RECORD_REMOTE_QUARTER
		val updatedRemoteQuarter = DEFAULT_RECORD_REMOTE_QUARTER.copy(
			grade = 85.0,
			gradeSum = 85.0,
			subjects = listOf(DEFAULT_RECORD_REMOTE_SUBJECT.copy(grade = 85))
		)
		val remoteDataSource = object : QuarterRemoteDataSource {
			override suspend fun getQuarters() = buildList {
				refreshStarted.complete(Unit)
				releaseStaleRefresh.await()
				add(staleRemoteQuarter)
			}

			override suspend fun getQuarter(qid: String) = staleRemoteQuarter

			override suspend fun removeQuarter(
				qid: String,
				mutationId: String,
				expectedRevision: Long
			) = error("unused")

			override suspend fun addQuarter(
				quarter: com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteQuarter,
				mutationId: String,
				expectedRevision: Long
			) = error("unused")

			override suspend fun setSubjectGrade(
				qid: String,
				sid: String,
				grade: Int,
				mutationId: String,
				expectedRevision: Long
			) = com.gdavidpb.tuindice.record.data.repository.quarter.model.RemoteSetSubjectGradeAck(
				mutationId = mutationId,
				subject = DEFAULT_RECORD_REMOTE_SUBJECT.copy(grade = grade),
				affectedQuarters = listOf(updatedRemoteQuarter)
			)
		}
		val repository = repository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = FakeQuarterSettingsDataSource(onCooldown = false)
		)

		val refreshJob = launch {
			repository.updateQuarters()
		}

		refreshStarted.await()

		repository.setSubjectGrade(
			SubjectGradeSet(
				quarterId = DEFAULT_RECORD_QUARTER.id,
				id = DEFAULT_RECORD_QUARTER.subjects.single().id,
				grade = 85,
				commit = true
			)
		)

		releaseStaleRefresh.complete(Unit)
		refreshJob.join()

		assertEquals(85, localDataSource.getQuarter(DEFAULT_RECORD_QUARTER.id)?.subjects?.single()?.grade)
	}

	@Test
	fun removeQuarter_whenRemoteSucceeds_confirmsLocalRemovalAndClearsPending() = runTest {
		val localDataSource = FakeQuarterLocalDataSource(
			initialQuarters = listOf(deletableLocalQuarter)
		)
		val remoteDataSource = FakeQuarterRemoteDataSource()
		val outboxRepository = FakeMutationOutboxRepository<RecordMutation>()
		val repository = repository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			outboxRepository = outboxRepository
		)

		repository.removeQuarter(QuarterRemove(id = deletableQuarter.id))

		assertEquals(1, remoteDataSource.removeQuarterCalls.size)
		assertEquals(deletableQuarter.id, remoteDataSource.removeQuarterCalls.single().quarterId)
		assertEquals(listOf(deletableQuarter.id), localDataSource.confirmedRemovedQuarterIds)
		assertTrue(outboxRepository.getPendingMutations().isEmpty())
	}

	@Test
	fun removeQuarter_whenRemoteFails_keepsPendingDeletionAndRethrows() = runTest {
		val localDataSource = FakeQuarterLocalDataSource(
			initialQuarters = listOf(deletableLocalQuarter)
		)
		val remoteDataSource = FakeQuarterRemoteDataSource(
			removeQuarterThrowable = IllegalStateException("boom")
		)
		val outboxRepository = FakeMutationOutboxRepository<RecordMutation>()
		val repository = repository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			outboxRepository = outboxRepository
		)

		assertFailsWith<IllegalStateException> {
			repository.removeQuarter(QuarterRemove(id = deletableQuarter.id))
		}

		assertTrue(localDataSource.confirmedRemovedQuarterIds.isEmpty())
		assertEquals(1, outboxRepository.getPendingMutations().size)
	}

	@Test
	fun removeQuarter_whenQuarterIsInstitutionalCurrent_doesNotEnqueueOrCallRemote() = runTest {
		val localDataSource = FakeQuarterLocalDataSource(
			initialQuarters = listOf(DEFAULT_RECORD_LOCAL_QUARTER)
		)
		val remoteDataSource = FakeQuarterRemoteDataSource()
		val outboxRepository = FakeMutationOutboxRepository<RecordMutation>()
		val repository = repository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			outboxRepository = outboxRepository
		)

		repository.removeQuarter(QuarterRemove(id = DEFAULT_RECORD_QUARTER.id))

		assertTrue(remoteDataSource.removeQuarterCalls.isEmpty())
		assertTrue(localDataSource.confirmedRemovedQuarterIds.isEmpty())
		assertTrue(outboxRepository.getPendingMutations().isEmpty())
	}

	private fun repository(
		localDataSource: FakeQuarterLocalDataSource,
		remoteDataSource: QuarterRemoteDataSource,
		settingsDataSource: FakeQuarterSettingsDataSource = FakeQuarterSettingsDataSource(onCooldown = true),
		outboxRepository: FakeMutationOutboxRepository<RecordMutation> = FakeMutationOutboxRepository()
	): QuarterDataRepository {
		return QuarterDataRepository(
			localDataSource = localDataSource,
			remoteDataSource = remoteDataSource,
			settingsDataSource = settingsDataSource,
			mutationOutboxRepository = outboxRepository,
			identifierRepository = FakeIdentifierRepository(identifier = "mutation-1")
		)
	}
}
