package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncReportSources
import com.gdavidpb.tuindice.base.domain.model.SyncReportStatus
import com.gdavidpb.tuindice.base.domain.model.SyncSourceReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.dialog_message_sync_sources_enrollment_unavailable
import tuindice.summary.generated.resources.dialog_message_sync_sources_record_and_enrollment_unavailable
import tuindice.summary.generated.resources.dialog_message_sync_sources_record_unavailable
import tuindice.summary.generated.resources.dialog_message_sync_unavailable
import kotlin.test.Test
import kotlin.test.assertEquals

class UnavailableSourcesMessageTest {
	@Test
	fun resolveUnavailableSourcesMessage_namesTheSourceThatCouldNotBeRead() {
		assertEquals(
			UiText.Resource(Res.string.dialog_message_sync_sources_record_unavailable),
			resolveUnavailableSourcesMessage(SyncReport.failedRecordUnavailable())
		)
		assertEquals(
			UiText.Resource(Res.string.dialog_message_sync_sources_enrollment_unavailable),
			resolveUnavailableSourcesMessage(SyncReport.partialEnrollmentUnavailable())
		)
		assertEquals(
			UiText.Resource(Res.string.dialog_message_sync_sources_record_and_enrollment_unavailable),
			resolveUnavailableSourcesMessage(
				report(record = SyncSourceStatus.Unavailable, enrollment = SyncSourceStatus.Unavailable)
			)
		)
	}

	@Test
	fun resolveUnavailableSourcesMessage_fallsBackToThePlainCopyWhenNoSourceIsNamed() {
		assertEquals(
			UiText.Resource(Res.string.dialog_message_sync_unavailable),
			resolveUnavailableSourcesMessage(SyncReport.success())
		)
	}

	private fun report(record: SyncSourceStatus, enrollment: SyncSourceStatus): SyncReport {
		return SyncReport(
			status = SyncReportStatus.Failed,
			sources = SyncReportSources(
				record = SyncSourceReport(record),
				enrollment = SyncSourceReport(enrollment)
			)
		)
	}
}
