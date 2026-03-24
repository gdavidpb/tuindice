package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutation
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.record.data.repository.mutation.RecordMutation
import com.gdavidpb.tuindice.record.testing.DEFAULT_RECORD_LOCAL_QUARTER
import com.gdavidpb.tuindice.record.utils.resolveQuarterSyncResolution
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuarterSyncResolutionTest {
	private val baseTimestamp = 1_767_225_600_000L

	@Test
	fun resolveQuarterSyncResolution_invalidatesMutationsForClosedQuarter() = runTest {
		val closedQuarter = DEFAULT_RECORD_LOCAL_QUARTER.copy(
			isReadOnly = true,
			subjects = emptyList()
		)
		val openQuarter = DEFAULT_RECORD_LOCAL_QUARTER.copy(
			id = "quarter-2",
			name = "2026-2",
			startDate = baseTimestamp + 100_000L,
			endDate = baseTimestamp + 200_000L,
			subjects = DEFAULT_RECORD_LOCAL_QUARTER.subjects.map { subject ->
				subject.copy(
					id = "subject-2",
					quarterId = "quarter-2"
				)
			}
		)
		val pendingMutations = listOf(
			pendingMutation(
				mutationId = "grade-closed",
				mutation = RecordMutation.SetSubjectGrade(
					quarterId = closedQuarter.id,
					subjectId = DEFAULT_RECORD_LOCAL_QUARTER.subjects.single().id,
					grade = 95
				)
			),
			pendingMutation(
				mutationId = "remove-closed",
				mutation = RecordMutation.RemoveQuarter(
					quarterId = closedQuarter.id
				)
			),
			pendingMutation(
				mutationId = "grade-open",
				mutation = RecordMutation.SetSubjectGrade(
					quarterId = openQuarter.id,
					subjectId = openQuarter.subjects.single().id,
					grade = 88
				)
			)
		)

		val result = resolveQuarterSyncResolution(
			incomingQuarters = listOf(closedQuarter, openQuarter),
			pendingMutations = pendingMutations
		)

		assertEquals(setOf(closedQuarter.id), result.replacedClosedQuarterIds)
		assertEquals(setOf("grade-closed", "remove-closed"), result.invalidatedMutationIds)
		assertEquals(listOf("grade-open"), result.compatiblePendingMutations.map { mutation -> mutation.mutationId })
	}

	@Test
	fun resolveQuarterSyncResolution_keepsPendingMutationsForOpenQuarters() = runTest {
		val openQuarter = DEFAULT_RECORD_LOCAL_QUARTER.copy(
			isReadOnly = false
		)
		val pendingMutations = listOf(
			pendingMutation(
				mutationId = "grade-open",
				mutation = RecordMutation.SetSubjectGrade(
					quarterId = openQuarter.id,
					subjectId = openQuarter.subjects.single().id,
					grade = 91
				)
			)
		)

		val result = resolveQuarterSyncResolution(
			incomingQuarters = listOf(openQuarter),
			pendingMutations = pendingMutations
		)

		assertTrue(result.replacedClosedQuarterIds.isEmpty())
		assertTrue(result.invalidatedMutationIds.isEmpty())
		assertEquals(pendingMutations, result.compatiblePendingMutations)
	}

	private fun pendingMutation(
		mutationId: String,
		mutation: RecordMutation
	): PendingMutation<RecordMutation> {
		return PendingMutation(
			mutationId = mutationId,
			mutation = mutation,
			expectedRevision = 1L,
			status = PendingMutationStatus.Pending,
			createdAt = baseTimestamp,
			updatedAt = baseTimestamp,
			lastError = null
		)
	}
}
