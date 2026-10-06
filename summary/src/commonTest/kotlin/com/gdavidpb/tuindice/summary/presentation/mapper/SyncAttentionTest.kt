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
	fun resolveSyncAttention_leavesWhatTheUniversityReportsToTheScreensThatOwnIt() {
		assertEquals(
			SyncAttention.None,
			resolveSyncAttention(SyncStatus.NewStudentNoRecord, SyncReport.success())
		)
		// The report that failure really carries marks the record source unavailable.
		assertEquals(
			SyncAttention.None,
			resolveSyncAttention(SyncStatus.NewStudentNoRecord, SyncReport.failedRecordUnavailable())
		)
		assertEquals(
			SyncAttention.None,
			resolveSyncAttention(SyncStatus.Healthy, report(SyncSourceStatus.NotEnrolled, null))
		)
		assertEquals(
			SyncAttention.None,
			resolveSyncAttention(SyncStatus.Healthy, report(SyncSourceStatus.Success, EnrollmentSituation("06")))
		)
		assertEquals(
			SyncAttention.None,
			resolveSyncAttention(SyncStatus.Healthy, report(SyncSourceStatus.NotEnrolled, EnrollmentSituation("06")))
		)
	}

	@Test
	fun resolveSyncAttention_keepsTheProblemWhenTheReportAlsoCarriesWhatTheUniversityReports() {
		val annulled = report(SyncSourceStatus.Success, EnrollmentSituation(code = "01"))
		val notEnrolled = report(SyncSourceStatus.NotEnrolled, null)

		assertEquals(SyncAttention.Problem, resolveSyncAttention(SyncStatus.Failed, annulled))
		assertEquals(SyncAttention.Problem, resolveSyncAttention(SyncStatus.OutdatedCredentials, notEnrolled))
		// The annulment a previous sync read is carried over when the enrollment cannot be read.
		assertEquals(
			SyncAttention.Problem,
			resolveSyncAttention(
				SyncStatus.Healthy,
				SyncReport.partialEnrollmentUnavailable().carryingEnrollmentFrom(annulled)
			)
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
		assertNull(resolveSyncAttentionKey(SyncStatus.Healthy, SyncReport.success()))
	}

	@Test
	fun resolveSyncAttentionKey_isNullForWhatTheUniversityReports() {
		assertNull(resolveSyncAttentionKey(SyncStatus.Healthy, report(SyncSourceStatus.NotEnrolled, null)))
		assertNull(
			resolveSyncAttentionKey(
				SyncStatus.Healthy,
				report(SyncSourceStatus.Success, EnrollmentSituation(code = "01"))
			)
		)
		assertNull(resolveSyncAttentionKey(SyncStatus.NewStudentNoRecord, SyncReport.failedRecordUnavailable()))
	}

	@Test
	fun resolveSyncAttentionKey_namesEveryProblem() {
		assertNotNull(resolveSyncAttentionKey(SyncStatus.Failed, SyncReport.failedRecordUnavailable()))
		assertNotNull(resolveSyncAttentionKey(SyncStatus.OutdatedCredentials, SyncReport.success()))
		assertNotNull(resolveSyncAttentionKey(SyncStatus.Healthy, SyncReport.partialEnrollmentUnavailable()))
	}

	@Test
	fun resolveSyncAttentionKey_staysTheSameAcrossSyncsThatRepeatTheProblem() {
		// Every sync stamps a new read instant on the report; the problem is still the same.
		assertEquals(
			resolveSyncAttentionKey(
				SyncStatus.Failed,
				SyncReport.failedRecordUnavailable().copy(enrollmentReadAt = 1_000L)
			),
			resolveSyncAttentionKey(
				SyncStatus.Failed,
				SyncReport.failedRecordUnavailable().copy(enrollmentReadAt = 2_000L)
			)
		)
	}

	@Test
	fun resolveSyncAttentionKey_changesWhenTheProblemChanges() {
		assertNotEquals(
			resolveSyncAttentionKey(SyncStatus.Failed, SyncReport.failedRecordUnavailable()),
			resolveSyncAttentionKey(SyncStatus.RecordAccessDenied, SyncReport.failedRecordUnavailable())
		)
		// The same status, another source that could not be read.
		assertNotEquals(
			resolveSyncAttentionKey(SyncStatus.Healthy, SyncReport.partialEnrollmentUnavailable()),
			resolveSyncAttentionKey(SyncStatus.Healthy, SyncReport.failedRecordUnavailable())
		)
	}

	@Test
	fun resolveSyncAttentionKey_ignoresWhatTheUniversityReportsWhileTheProblemLasts() {
		val enrolled = report(SyncSourceStatus.Success, null)
		val notEnrolled = report(SyncSourceStatus.NotEnrolled, null)
		val annulled = report(SyncSourceStatus.Success, EnrollmentSituation(code = "01"))
		val annulledForAnotherCause = report(SyncSourceStatus.Success, EnrollmentSituation(code = "06"))

		// The row announces the failed sync; the enrollment it last read is not part of that.
		assertEquals(
			resolveSyncAttentionKey(SyncStatus.Failed, enrolled),
			resolveSyncAttentionKey(SyncStatus.Failed, notEnrolled)
		)
		assertEquals(
			resolveSyncAttentionKey(SyncStatus.Failed, enrolled),
			resolveSyncAttentionKey(SyncStatus.Failed, annulled)
		)
		assertEquals(
			resolveSyncAttentionKey(SyncStatus.Failed, annulled),
			resolveSyncAttentionKey(SyncStatus.Failed, annulledForAnotherCause)
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
