package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTermPeriod
import com.gdavidpb.tuindice.academiccore.domain.model.AttemptScore
import com.gdavidpb.tuindice.base.domain.model.mutation.PendingMutationStatus
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationEnvelope
import com.gdavidpb.tuindice.persistence.domain.mutation.MutationPrecondition
import com.gdavidpb.tuindice.persistence.domain.record.AcademicRecordMutation
import com.gdavidpb.tuindice.persistence.domain.record.RECORD_MUTATION_SCOPE
import com.gdavidpb.tuindice.record.domain.model.RecordRejection
import com.gdavidpb.tuindice.record.domain.model.RecordRejectionKind
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicRecordRejectionKindTest {
	@Test
	fun rejectedEdits_areToldApartByWhatTheyWereAbout() = runTest {
		val dataSource = AcademicRecordDataSource(
			localDataSource = FakeAcademicRecordLocalDataRepository(record = defaultVersionedRecord()),
			remoteDataSource = ControlledAcademicRecordRemoteDataRepository(defaultVersionedRecord()),
			settingsDataSource = FakeRecordSettingsDataRepository(),
			mutationEngine = createMutationEngine(
				this,
				outboxStore = InMemoryMutationEnvelopeStore(
					listOf(
						rejected(
							"grade",
							AcademicRecordMutation.UpsertAttemptOverride(
								attemptId = "attempt-1",
								score = AttemptScore.numeric(4),
								outcome = null
							)
						),
						rejected("grade-delete", AcademicRecordMutation.DeleteAttemptOverride("attempt-2")),
						rejected("term", AcademicRecordMutation.DeleteSyntheticTerm("2026-JUL_AUG")),
						rejected(
							"term-update",
							AcademicRecordMutation.UpdateSyntheticTerm(
								targetTermId = "2026-JUL_AUG",
								targetTermKey = "2026-JUL_AUG",
								termId = "2026-JUL_AUG",
								periodYear = 2026,
								periodCode = AcademicTermPeriod.JUL_AUG,
								attempts = emptyList()
							)
						)
					)
				)
			),
			identifierRepository = FakeIdentifierRepository()
		)

		assertEquals(
			listOf(
				RecordRejection("grade", RecordRejectionKind.Grade),
				RecordRejection("grade-delete", RecordRejectionKind.Grade),
				RecordRejection("term", RecordRejectionKind.Term),
				RecordRejection("term-update", RecordRejectionKind.Term)
			),
			dataSource.observeTerminallyRejectedMutationsFlow().first()
		)
	}

	private fun rejected(
		id: String,
		command: AcademicRecordMutation
	): MutationEnvelope<String, AcademicRecordMutation> = MutationEnvelope(
		mutationId = id,
		scopeKey = RECORD_MUTATION_SCOPE,
		command = command,
		precondition = MutationPrecondition.Revision(1L),
		status = PendingMutationStatus.FailedTerminal,
		createdAt = 1L,
		updatedAt = 1L,
		lastError = "rejected"
	)
}
