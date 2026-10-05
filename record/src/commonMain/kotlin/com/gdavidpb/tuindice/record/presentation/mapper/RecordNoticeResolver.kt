package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.isCurrent
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.mapper.EnrollmentAnnulmentTexts
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.ObservedRecord
import com.gdavidpb.tuindice.record.presentation.model.RecordNotice
import com.gdavidpb.tuindice.record.presentation.model.RecordNoticeKind
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.record_notice_stale_message
import tuindice.record.generated.resources.record_notice_stale_message_unknown

// One notice at a time, annulled before stale. The moment of an annulment is whether the local
// record still has a current term (provisional keeps it, final has dropped it), never a stored flag.
internal fun resolveRecordNotice(observed: ObservedRecord): RecordNotice? {
	val enrollment = observed.syncReport.sources.enrollment
	val hasCurrentTerm = observed.record.terms.any { term -> term.kind.isCurrent }
	val situation = enrollment.situation

	return when {
		situation != null -> {
			val isProvisional = hasCurrentTerm

			RecordNotice(
				title = EnrollmentAnnulmentTexts.title(isProvisional = isProvisional),
				message = EnrollmentAnnulmentTexts.message(
					cause = situation.annulmentCause,
					isProvisional = isProvisional
				),
				kind = if (isProvisional) {
					RecordNoticeKind.AnnulledProvisional
				} else {
					RecordNoticeKind.AnnulledFinal
				}
			)
		}

		// Unavailable: the enrollment could not be read. NotAttempted: the sync failed before
		// getting to it. Either way the current term on screen is the one an earlier sync left.
		enrollment.status.isNotRefreshed && hasCurrentTerm -> {
			val readAt = observed.syncReport.enrollmentReadAt
				?.formatDate(DateTextStyle.DAY_SHORT_MONTH)

			RecordNotice(
				title = null,
				message = if (readAt != null) {
					UiText.Resource(Res.string.record_notice_stale_message, listOf(readAt))
				} else {
					UiText.Resource(Res.string.record_notice_stale_message_unknown)
				},
				kind = RecordNoticeKind.StaleEnrollment
			)
		}

		else -> null
	}
}

private val SyncSourceStatus.isNotRefreshed: Boolean
	get() = this == SyncSourceStatus.Unavailable || this == SyncSourceStatus.NotAttempted
