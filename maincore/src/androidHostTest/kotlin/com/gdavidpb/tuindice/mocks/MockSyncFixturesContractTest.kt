package com.gdavidpb.tuindice.mocks

import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.EnrollmentAnnulmentCause
import com.gdavidpb.tuindice.base.domain.model.SyncReportStatus
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.data.model.SyncResult
import com.gdavidpb.tuindice.data.source.sync.api.mapper.toSyncReport
import com.gdavidpb.tuindice.data.source.sync.api.mapper.toSyncResult
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncRecordErrorResponse
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncRecordResponse
import com.gdavidpb.tuindice.di.createSharedJson
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * The WireMock sync fixtures must stay decodable by the production DTOs, in both shapes a server
 * can answer with: the one deployed today (an annulled enrollment with no current term) and the
 * one that follows the provisional window rule (the same situation while the current term stays).
 */
class MockSyncFixturesContractTest {
	private val json = createSharedJson()

	@Test
	fun notEnrolledDecodesTheNewSourceStatusWithoutACurrentTerm() {
		val result = decodeSync("post-sync-not-enrolled.json")

		assertEquals(SyncSourceStatus.NotEnrolled, result.sync.sources.enrollment.status)
		assertTrue(result.hasNoCurrentTerm())
	}

	@Test
	fun annulledProvisionalKeepsTheCurrentTermWithItsSchedule() {
		val result = decodeSync("post-sync-annulled-provisional.json")
		val situation = assertNotNull(result.sync.sources.enrollment.situation)
		val current = result.record.record.terms.single { term -> term.kind == TermKind.CURRENT }
		val scheduled = current.attempts.filter { attempt -> !attempt.schedule.isNullOrEmpty() }

		assertEquals(EnrollmentAnnulmentCause.AcademicIndex, situation.annulmentCause)
		assertEquals(2, scheduled.size)
		assertEquals(1, scheduled.first().section)
		assertEquals("MYS-116", scheduled.first().schedule?.first()?.classroom)
		assertEquals(listOf("CHOQUE DE HORARIO"), scheduled.last().enrollmentErrors)
		assertTrue(current.attempts.any { attempt -> attempt.schedule.isNullOrEmpty() })
	}

	@Test
	fun annulledFinalHasTheSituationAndNoCurrentTerm() {
		val result = decodeSync("post-sync-annulled-final.json")

		assertNotNull(result.sync.sources.enrollment.situation)
		assertTrue(result.hasNoCurrentTerm())
	}

	@Test
	fun everySuccessfulSyncFixtureDecodes() {
		val fixtures = File(SYNC_FIXTURES).listFiles { file -> file.name.startsWith("post-sync-") }.orEmpty()
			.filter { file -> "\"record\"" in file.readText() && "\"user\"" in file.readText() }

		assertTrue(
			fixtures.size >= MIN_SUCCESSFUL_FIXTURES,
			"Expected the sync fixtures, found ${fixtures.map(File::getName)}"
		)

		fixtures.forEach { fixture ->
			json.decodeFromString<SyncRecordResponse>(fixture.readText()).toSyncResult()
		}
	}

	@Test
	fun errorBodiesCarryTheReasonTheAppReads() {
		val newStudent = decodeError("post-sync-new-student-no-record.json")
		val denied = decodeError("post-sync-record-access-denied.json")

		assertEquals("NEW_STUDENT_NO_RECORD", newStudent.reason)
		assertEquals("DST_RECORD_ACCESS_DENIED", denied.reason)
		assertEquals(SyncReportStatus.Failed, newStudent.sync.toSyncReport().status)
		assertNull(decodeError("post-sync-outdated-credentials.json").reason?.takeIf { it.isBlank() })
	}

	private fun SyncResult.hasNoCurrentTerm() = record.record.terms.none { term -> term.kind == TermKind.CURRENT }

	private fun decodeSync(name: String): SyncResult =
		json.decodeFromString<SyncRecordResponse>(File("$SYNC_FIXTURES/$name").readText()).toSyncResult()

	private fun decodeError(name: String): SyncRecordErrorResponse =
		json.decodeFromString<SyncRecordErrorResponse>(File("$SYNC_FIXTURES/$name").readText())

	private companion object {
		// Host tests of this module run from its directory.
		const val SYNC_FIXTURES = "../mocks/__files/sync"
		const val MIN_SUCCESSFUL_FIXTURES = 8
	}
}
