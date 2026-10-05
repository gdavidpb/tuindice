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
