package com.gdavidpb.tuindice.data.source.assistant

import com.gdavidpb.tuindice.chatcontract.context.ClientState
import com.gdavidpb.tuindice.chatcontract.context.PendingMutationRef
import com.gdavidpb.tuindice.data.source.pending.PendingChangesDataSource
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_SCOPE
import com.gdavidpb.tuindice.evaluations.data.mutation.EVALUATIONS_MUTATION_STORE_ID
import com.gdavidpb.tuindice.persistence.data.room.entity.PendingMutationEntity
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_STORE_ID
import com.gdavidpb.tuindice.record.domain.usecase.GetRecordRevisionUseCase
import com.gdavidpb.tuindice.testkit.base.repository.FakeSyncStatusRepository
import com.gdavidpb.tuindice.testkit.base.repository.RecordingReportingRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AssistantClientStateDataSourceTest {
	@Test
	fun clientState_carriesTheRecordRevisionAndTheMutationsTheOutboxHolds() = runTest {
		val source = clientStateSource(
			records = FakeRecordRepository(revision = 7L),
			outbox = listOf(
				pendingMutation("r1", RECORD_MUTATION_STORE_ID, RECORD_MUTATION_SCOPE, status = "Pending"),
				pendingMutation("e1", EVALUATIONS_MUTATION_STORE_ID, EVALUATIONS_MUTATION_SCOPE, status = "Failed")
			)
		)

		assertEquals(
			ClientState(
				recordRevision = 7L,
				pendingMutations = listOf(
					PendingMutationRef(mutationId = "r1", scope = RECORD_MUTATION_SCOPE, status = "Pending"),
					PendingMutationRef(mutationId = "e1", scope = EVALUATIONS_MUTATION_SCOPE, status = "Failed")
				)
			),
			source.clientState()
		)
	}

	@Test
	fun clientState_sendsTheRevisionAsUnknownWhenItCannotBeRead() = runTest {
		val source = clientStateSource(
			records = FakeRecordRepository(revisionFailure = IllegalStateException("the record is locked")),
			outbox = emptyList()
		)

		assertEquals(ClientState(recordRevision = null, pendingMutations = emptyList()), source.clientState())
	}

	private fun clientStateSource(
		records: FakeRecordRepository,
		outbox: List<PendingMutationEntity>
	): AssistantClientStateDataSource {
		val reporting = RecordingReportingRepository()
		val pendingChanges = PendingChangesDataSource(
			pendingMutationDao = FakePendingMutationDao(outbox),
			academicRecordRepository = records,
			evaluationRepository = FakeEvaluationRepository(),
			syncStatusRepository = FakeSyncStatusRepository()
		)

		return AssistantClientStateDataSource(
			getRecordRevisionUseCase = GetRecordRevisionUseCase(
				academicRecordRepository = records,
				reportingRepository = reporting
			),
			pendingChangesDataSource = pendingChanges
		)
	}
}
