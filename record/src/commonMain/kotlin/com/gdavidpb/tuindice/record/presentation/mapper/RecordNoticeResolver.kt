package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.ui.graphics.vector.ImageVector
import com.gdavidpb.tuindice.academiccore.domain.model.isCurrent
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.EnrollmentAnnulmentTexts
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.ObservedRecord
import com.gdavidpb.tuindice.record.presentation.model.RecordNotice
import com.gdavidpb.tuindice.record.presentation.model.RecordNoticeKind
import com.gdavidpb.tuindice.record.presentation.model.RecordNoticePlacement
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

			recordNotice(
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

			recordNotice(
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

// The notice the pager draws above its pages.
internal fun RecordNotice.takeIfAbovePager(): RecordNotice? {
	return takeIf { placement == RecordNoticePlacement.AbovePager }
}

// The notice a term's page draws: only the current term's, and only what speaks of that term.
internal fun RecordNotice.takeIfOnTermPage(isCurrentTerm: Boolean): RecordNotice? {
	return takeIf { isCurrentTerm && placement == RecordNoticePlacement.CurrentTermPage }
}

// Place and icon follow from what the notice is about, so no view has to ask its kind.
internal fun recordNotice(
	title: UiText?,
	message: UiText,
	kind: RecordNoticeKind
) = RecordNotice(
	title = title,
	message = message,
	kind = kind,
	placement = kind.placement,
	icon = kind.icon
)

// A final annulment has no current term to sit on; everything else describes the current term.
private val RecordNoticeKind.placement: RecordNoticePlacement
	get() = when (this) {
		RecordNoticeKind.AnnulledFinal -> RecordNoticePlacement.AbovePager

		RecordNoticeKind.AnnulledProvisional,
		RecordNoticeKind.StaleEnrollment,
		-> RecordNoticePlacement.CurrentTermPage
	}

// Stale data is a matter of time; an annulment is something to read.
private val RecordNoticeKind.icon: ImageVector
	get() = when (this) {
		RecordNoticeKind.StaleEnrollment -> Icons.Outlined.Schedule

		RecordNoticeKind.AnnulledProvisional,
		RecordNoticeKind.AnnulledFinal,
		-> Icons.Outlined.Info
	}

private val SyncSourceStatus.isNotRefreshed: Boolean
	get() = this == SyncSourceStatus.Unavailable || this == SyncSourceStatus.NotAttempted
