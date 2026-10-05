package com.gdavidpb.tuindice.summary.presentation.mapper

import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.presentation.model.UiText
import tuindice.summary.generated.resources.Res
import tuindice.summary.generated.resources.dialog_message_sync_sources_enrollment_unavailable
import tuindice.summary.generated.resources.dialog_message_sync_sources_record_and_enrollment_unavailable
import tuindice.summary.generated.resources.dialog_message_sync_sources_record_unavailable
import tuindice.summary.generated.resources.dialog_message_sync_unavailable

// Which of the university's services a sync could not read, said by name so the user knows what
// is stale; a report that names none falls back to the plain "unavailable" copy.
fun resolveUnavailableSourcesMessage(syncReport: SyncReport): UiText {
	val recordUnavailable = syncReport.sources.record.status == SyncSourceStatus.Unavailable
	val enrollmentUnavailable = syncReport.sources.enrollment.status == SyncSourceStatus.Unavailable

	return UiText.Resource(
		when {
			recordUnavailable && enrollmentUnavailable ->
				Res.string.dialog_message_sync_sources_record_and_enrollment_unavailable

			recordUnavailable ->
				Res.string.dialog_message_sync_sources_record_unavailable

			enrollmentUnavailable ->
				Res.string.dialog_message_sync_sources_enrollment_unavailable

			else ->
				Res.string.dialog_message_sync_unavailable
		}
	)
}
