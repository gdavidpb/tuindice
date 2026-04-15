package com.gdavidpb.tuindice.persistence.data.room.mapper

import com.gdavidpb.tuindice.base.domain.model.mutation.OutboxMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class MutationEnvelopeMapperTest {
	private val json = Json

	@Test
	fun roundTrip_preservesRevisionPrecondition() {
		val envelope = testEnvelope(
			mutationId = "mutation-1",
			precondition = MutationPrecondition.Revision(7L)
		)

		val restored = envelope
			.toPendingMutationEntity(
				storeId = "record",
				commandSerializer = TestMutation.serializer(),
				json = json
			)
			.toMutationEnvelope(
				commandSerializer = TestMutation.serializer(),
				json = json
			)

		assertEquals(envelope, restored)
	}

	@Test
	fun roundTrip_preservesNonePrecondition() {
		val envelope = testEnvelope(
			mutationId = "mutation-2",
			precondition = MutationPrecondition.None
		)

		val restored = envelope
			.toPendingMutationEntity(
				storeId = "record",
				commandSerializer = TestMutation.serializer(),
				json = json
			)
			.toMutationEnvelope(
				commandSerializer = TestMutation.serializer(),
				json = json
			)

		assertEquals(envelope, restored)
	}
}

private fun testEnvelope(
	mutationId: String,
	precondition: MutationPrecondition
) = MutationEnvelope(
	mutationId = mutationId,
	scopeKey = "record",
	command = TestMutation(
		entityId = "subject-1",
		grade = 95,
		replaceKey = "subject:subject-1"
	),
	precondition = precondition,
	status = PendingMutationStatus.Pending,
	createdAt = 10L,
	updatedAt = 10L,
	lastError = null,
	replaceKey = "subject:subject-1"
)

@Serializable
private data class TestMutation(
	override val entityId: String,
	val grade: Int,
	override val replaceKey: String
) : OutboxMutation {
	override val entityType: String = "test:set_subject_grade"
}
