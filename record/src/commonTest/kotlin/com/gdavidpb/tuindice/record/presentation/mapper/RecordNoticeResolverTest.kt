package com.gdavidpb.tuindice.record.presentation.mapper

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.base.domain.model.EnrollmentAnnulmentCause
import com.gdavidpb.tuindice.base.domain.model.EnrollmentSituation
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.base.domain.model.SyncReportSources
import com.gdavidpb.tuindice.base.domain.model.SyncReportStatus
import com.gdavidpb.tuindice.base.domain.model.SyncSourceReport
import com.gdavidpb.tuindice.base.domain.model.SyncSourceStatus
import com.gdavidpb.tuindice.base.domain.model.SyncStatus
import com.gdavidpb.tuindice.base.presentation.mapper.DateTextStyle
import com.gdavidpb.tuindice.base.presentation.mapper.EnrollmentAnnulmentTexts
import com.gdavidpb.tuindice.base.presentation.mapper.formatDate
import com.gdavidpb.tuindice.base.presentation.model.UiText
import com.gdavidpb.tuindice.record.domain.model.ObservedRecord
import com.gdavidpb.tuindice.record.domain.model.RecordViewMode
import com.gdavidpb.tuindice.record.presentation.model.RecordNoticeKind
import com.gdavidpb.tuindice.record.presentation.model.RecordNoticePlacement
import com.gdavidpb.tuindice.record.testing.academicTerm
import tuindice.record.generated.resources.Res
import tuindice.record.generated.resources.record_notice_stale_message
import tuindice.record.generated.resources.record_notice_stale_message_unknown
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RecordNoticeResolverTest {
	@Test
	fun resolve_placesWhatSpeaksOfTheCurrentTermOnItsPage_andAFinalAnnulmentAboveThePager() {
		val provisional = assertNotNull(
			resolveRecordNotice(
				observed(report = report(situation = EnrollmentSituation(code = "01")), hasCurrentTerm = true)
			)
		)
		val final = assertNotNull(
			resolveRecordNotice(
				observed(report = report(situation = EnrollmentSituation(code = "01")), hasCurrentTerm = false)
			)
		)

		assertEquals(RecordNoticePlacement.CurrentTermPage, provisional.placement)
		assertEquals(provisional, provisional.takeIfOnTermPage(isCurrentTerm = true))
		assertNull(provisional.takeIfOnTermPage(isCurrentTerm = false))
		assertNull(provisional.takeIfAbovePager())

		assertEquals(RecordNoticePlacement.AbovePager, final.placement)
		assertEquals(final, final.takeIfAbovePager())
		assertNull(final.takeIfOnTermPage(isCurrentTerm = true))
	}

	@Test
	fun resolve_givesStaleDataTheClockIcon_andAnAnnulmentTheInfoOne() {
		val stale = resolveRecordNotice(
			observed(report = report(enrollmentStatus = SyncSourceStatus.Unavailable), hasCurrentTerm = true)
		)
		val annulled = resolveRecordNotice(
			observed(report = report(situation = EnrollmentSituation(code = "01")), hasCurrentTerm = true)
		)

		assertEquals(Icons.Outlined.Schedule, stale?.icon)
		assertEquals(RecordNoticePlacement.CurrentTermPage, stale?.placement)
		assertEquals(Icons.Outlined.Info, annulled?.icon)
	}

	@Test
	fun resolve_whenSituationAndCurrentTerm_isProvisionalWithCauseCopy() {
		val notice = resolveRecordNotice(
			observed(report = report(situation = EnrollmentSituation(code = "01")), hasCurrentTerm = true)
		)

		assertEquals(RecordNoticeKind.AnnulledProvisional, notice?.kind)
		assertEquals(EnrollmentAnnulmentTexts.title(isProvisional = true), notice?.title)
		assertEquals(
			EnrollmentAnnulmentTexts.message(
				cause = EnrollmentAnnulmentCause.CreditLimit,
				isProvisional = true
			),
			notice?.message
		)
	}

	@Test
	fun resolve_whenSituationAndNoCurrentTerm_isFinal() {
		val notice = resolveRecordNotice(
			observed(report = report(situation = EnrollmentSituation(code = "99")), hasCurrentTerm = false)
		)

		assertEquals(RecordNoticeKind.AnnulledFinal, notice?.kind)
		assertEquals(EnrollmentAnnulmentTexts.title(isProvisional = false), notice?.title)
		assertEquals(
			EnrollmentAnnulmentTexts.message(cause = EnrollmentAnnulmentCause.Other, isProvisional = false),
			notice?.message
		)
	}

	@Test
	fun resolve_whenEnrollmentUnavailableWithCurrentTerm_isStaleWithoutTitle() {
		val notice = resolveRecordNotice(
			observed(
				report = report(enrollmentStatus = SyncSourceStatus.Unavailable),
				hasCurrentTerm = true
			)
		)

		assertEquals(RecordNoticeKind.StaleEnrollment, notice?.kind)
		assertNull(notice?.title)
	}

	@Test
	fun resolve_whenStaleAndTheLastReadIsKnown_describesTheMessageWithItsDayAndShortMonth() {
		val readAt = 1_767_625_200_000L
		val notice = resolveRecordNotice(
			observed(
				report = report(enrollmentStatus = SyncSourceStatus.Unavailable).copy(enrollmentReadAt = readAt),
				hasCurrentTerm = true
			)
		)

		assertEquals(
			UiText.Resource(
				Res.string.record_notice_stale_message,
				listOf(readAt.formatDate(DateTextStyle.DAY_SHORT_MONTH))
			),
			notice?.message
		)
	}

	@Test
	fun resolve_whenStaleAndTheLastReadIsUnknown_describesTheMessageWithoutADate() {
		val notice = resolveRecordNotice(
			observed(
				report = report(enrollmentStatus = SyncSourceStatus.Unavailable),
				hasCurrentTerm = true
			)
		)

		assertEquals(UiText.Resource(Res.string.record_notice_stale_message_unknown), notice?.message)
	}

	@Test
	fun resolve_whenTheSyncFailedBeforeReadingTheEnrollment_isStaleToo() {
		val notice = resolveRecordNotice(
			observed(
				report = report(enrollmentStatus = SyncSourceStatus.NotAttempted),
				hasCurrentTerm = true
			)
		)

		assertEquals(RecordNoticeKind.StaleEnrollment, notice?.kind)
	}

	@Test
	fun resolve_whenAnnulledAndUnavailable_annulmentWins() {
		val notice = resolveRecordNotice(
			observed(
				report = report(
					enrollmentStatus = SyncSourceStatus.Unavailable,
					situation = EnrollmentSituation(code = "06")
				),
				hasCurrentTerm = true
			)
		)

		assertEquals(RecordNoticeKind.AnnulledProvisional, notice?.kind)
	}

	@Test
	fun resolve_whenNothingToExplain_isNull() {
		assertNull(resolveRecordNotice(observed(report = report(), hasCurrentTerm = true)))
		assertNull(
			resolveRecordNotice(
				observed(report = report(enrollmentStatus = SyncSourceStatus.Unavailable), hasCurrentTerm = false)
			)
		)
		assertNull(
			resolveRecordNotice(
				observed(report = report(enrollmentStatus = SyncSourceStatus.Unknown), hasCurrentTerm = true)
			)
		)
	}

	private fun report(
		enrollmentStatus: SyncSourceStatus = SyncSourceStatus.Success,
		situation: EnrollmentSituation? = null
	): SyncReport {
		return SyncReport(
			status = SyncReportStatus.Success,
			sources = SyncReportSources(
				record = SyncSourceReport(SyncSourceStatus.Success),
				enrollment = SyncSourceReport(status = enrollmentStatus, situation = situation)
			)
		)
	}

	private fun observed(report: SyncReport, hasCurrentTerm: Boolean): ObservedRecord {
		return ObservedRecord(
			record = AcademicRecord(
				id = "record",
				terms = listOf(
					academicTerm(
						id = "term",
						kind = if (hasCurrentTerm) TermKind.CURRENT else TermKind.HISTORICAL
					)
				)
			),
			viewMode = RecordViewMode.Projection,
			selectedTermId = "term",
			hasSyncedRecord = true,
			syncStatus = SyncStatus.Healthy,
			syncReport = report
		)
	}
}
