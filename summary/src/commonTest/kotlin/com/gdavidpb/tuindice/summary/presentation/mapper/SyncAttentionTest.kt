package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.EnrollmentSituation
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncReportSources
import com.gdavidpb.tuindice.base.domain.model.SyncReportStatus
import com.gdavidpb.tuindice.base.domain.model.SyncSourceReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.summary.presentation.model.SyncAttention
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SyncAttentionTest {
	@Test
	fun resolveSyncAttention_treatsFailuresAndDeniedAccessAsProblems() {
		listOf(
			SyncStatus.Unavailable,
			SyncStatus.Failed,
			SyncStatus.OutdatedCredentials,
			SyncStatus.MissingCredentials,
			SyncStatus.RecordAccessDenied
		).forEach { status ->
			assertEquals(SyncAttention.Problem, resolveSyncAttention(status, SyncReport.success()))
		}
		assertEquals(
			SyncAttention.Problem,
			resolveSyncAttention(SyncStatus.Healthy, SyncReport.partialEnrollmentUnavailable())
		)
	}

	@Test
	fun resolveSyncAttention_treatsWhatTheUniversityReportsAsInformative() {
		assertEquals(
			SyncAttention.Informative,
			resolveSyncAttention(SyncStatus.NewStudentNoRecord, SyncReport.success())
		)
		// The report that failure really carries marks the record source unavailable.
		assertEquals(
			SyncAttention.Informative,
			resolveSyncAttention(SyncStatus.NewStudentNoRecord, SyncReport.failedRecordUnavailable())
		)
		assertEquals(
			SyncAttention.Informative,
			resolveSyncAttention(SyncStatus.Healthy, report(SyncSourceStatus.NotEnrolled, null))
		)
		assertEquals(
			SyncAttention.Informative,
			resolveSyncAttention(SyncStatus.Healthy, report(SyncSourceStatus.Success, EnrollmentSituation("06")))
		)
	}

	@Test
	fun resolveSyncAttention_ignoresAHealthyAndAnUnknownReport() {
		assertEquals(SyncAttention.None, resolveSyncAttention(SyncStatus.Healthy, SyncReport.success()))
		assertEquals(
			SyncAttention.None,
			resolveSyncAttention(SyncStatus.Healthy, report(SyncSourceStatus.Unknown, null))
		)
	}

	@Test
	fun resolveSyncAttentionKey_isNullWhenTheRowAnnouncesNothing() {
		assertNull(resolveSyncAttentionKey(SyncStatus.Healthy, SyncReport.success(), hasCurrentTerm = true))
	}

	@Test
	fun resolveSyncAttentionKey_namesInformativeAnnouncementsToo() {
		assertNotNull(
			resolveSyncAttentionKey(
				SyncStatus.Healthy,
				report(SyncSourceStatus.NotEnrolled, null),
				hasCurrentTerm = false
			)
		)
		assertNotNull(
			resolveSyncAttentionKey(
				SyncStatus.NewStudentNoRecord,
				SyncReport.failedRecordUnavailable(),
				hasCurrentTerm = false
			)
		)
	}

	@Test
	fun resolveSyncAttentionKey_staysTheSameAcrossSyncsThatRepeatTheAnnouncement() {
		val annulled = report(SyncSourceStatus.Success, EnrollmentSituation(code = "01"))

		// Every sync stamps a new read instant on the report; the announcement is still the same.
		assertEquals(
			resolveSyncAttentionKey(SyncStatus.Healthy, annulled.readAt(1_000L), hasCurrentTerm = true),
			resolveSyncAttentionKey(SyncStatus.Healthy, annulled.readAt(2_000L), hasCurrentTerm = true)
		)
		assertEquals(
			resolveSyncAttentionKey(
				SyncStatus.Failed,
				SyncReport.failedRecordUnavailable().copy(enrollmentReadAt = 1_000L),
				hasCurrentTerm = true
			),
			resolveSyncAttentionKey(
				SyncStatus.Failed,
				SyncReport.failedRecordUnavailable().copy(enrollmentReadAt = 2_000L),
				hasCurrentTerm = true
			)
		)
	}

	@Test
	fun resolveSyncAttentionKey_changesWhenAProvisionalAnnulmentBecomesFinal() {
		val annulled = report(SyncSourceStatus.Success, EnrollmentSituation(code = "01"))

		assertNotEquals(
			resolveSyncAttentionKey(SyncStatus.Healthy, annulled, hasCurrentTerm = true),
			resolveSyncAttentionKey(SyncStatus.Healthy, annulled, hasCurrentTerm = false)
		)
	}

	@Test
	fun resolveSyncAttentionKey_changesWhenTheCauseOfTheAnnulmentChanges() {
		assertNotEquals(
			resolveSyncAttentionKey(
				SyncStatus.Healthy,
				report(SyncSourceStatus.Success, EnrollmentSituation(code = "01")),
				hasCurrentTerm = true
			),
			resolveSyncAttentionKey(
				SyncStatus.Healthy,
				report(SyncSourceStatus.Success, EnrollmentSituation(code = "06")),
				hasCurrentTerm = true
			)
		)
	}

	@Test
	fun resolveSyncAttentionKey_changesWhenInformationBecomesAProblem() {
		val notEnrolled = report(SyncSourceStatus.NotEnrolled, null)

		assertNotEquals(
			resolveSyncAttentionKey(SyncStatus.Healthy, notEnrolled, hasCurrentTerm = false),
			resolveSyncAttentionKey(SyncStatus.Failed, notEnrolled, hasCurrentTerm = false)
		)
	}

	@Test
	fun resolveSyncAttentionKey_ignoresTheCurrentTermWhenNoAnnulmentIsRead() {
		val notEnrolled = report(SyncSourceStatus.NotEnrolled, null)
		val annulled = report(SyncSourceStatus.Success, EnrollmentSituation(code = "01"))

		assertEquals(
			resolveSyncAttentionKey(SyncStatus.Healthy, notEnrolled, hasCurrentTerm = true),
			resolveSyncAttentionKey(SyncStatus.Healthy, notEnrolled, hasCurrentTerm = false)
		)
		// A problem explains itself, not the annulment the report still carries.
		assertEquals(
			resolveSyncAttentionKey(SyncStatus.Failed, annulled, hasCurrentTerm = true),
			resolveSyncAttentionKey(SyncStatus.Failed, annulled, hasCurrentTerm = false)
		)
	}

	private fun report(enrollment: SyncSourceStatus, situation: EnrollmentSituation?): SyncReport {
		return SyncReport(
			status = SyncReportStatus.Success,
			sources = SyncReportSources(
				record = SyncSourceReport(SyncSourceStatus.Success),
				enrollment = SyncSourceReport(enrollment, situation)
			)
		)
	}
}
