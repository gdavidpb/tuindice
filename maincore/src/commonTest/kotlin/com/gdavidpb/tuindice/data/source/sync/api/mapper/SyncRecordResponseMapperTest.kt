package com.gdavidpb.tuindice.data.source.sync.api.mapper

import com.gdavidpb.tuindice.academiccore.domain.model.AcademicProfile
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicRecord
import com.gdavidpb.tuindice.base.domain.model.SyncReport
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncRecordResponse
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncReportResponse
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncReportSourcesResponse
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncReportStatusResponse
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncSourceReportResponse
import com.gdavidpb.tuindice.data.source.sync.api.response.SyncSourceStatusResponse
import com.gdavidpb.tuindice.record.data.source.api.response.AcademicRecordResponse
import com.gdavidpb.tuindice.summary.data.model.GetUserResponse
import kotlin.test.Test
import kotlin.test.assertEquals

class SyncRecordResponseMapperTest {
	@Test
	fun toSyncResult_defaultsToSuccess_whenSyncReportIsAbsent() {
		val result = syncRecordResponse(sync = null).toSyncResult()

		assertEquals(SyncReport.success(), result.sync)
	}

	@Test
	fun toSyncResult_mapsSuccessfulSyncReport() {
		val result = syncRecordResponse(
			sync = syncReportResponse(
				status = SyncReportStatusResponse.Success,
				record = SyncSourceStatusResponse.Success,
				enrollment = SyncSourceStatusResponse.Success
			)
		).toSyncResult()

		assertEquals(SyncReport.success(), result.sync)
	}

	@Test
	fun toSyncResult_mapsPartialSyncReport() {
		val result = syncRecordResponse(
			sync = syncReportResponse(
				status = SyncReportStatusResponse.Partial,
				record = SyncSourceStatusResponse.Success,
				enrollment = SyncSourceStatusResponse.Unavailable
			)
		).toSyncResult()

		assertEquals(SyncReport.partialEnrollmentUnavailable(), result.sync)
	}

	private fun syncRecordResponse(sync: SyncReportResponse?): SyncRecordResponse {
		return SyncRecordResponse(
			record = AcademicRecordResponse(
				revision = 1L,
				record = AcademicRecord(
					id = "record-id",
					profile = AcademicProfile(
						userId = "user-id",
						identityCardNumber = 12345678,
						usbId = "12-34567",
						email = "12-34567@usb.ve",
						firstNames = "Ada",
						lastNames = "Lovelace",
						careerName = "Ingenieria",
						careerCode = 12039,
						scholarship = false
					),
					terms = emptyList(),
					attemptOverrides = emptyList()
				)
			),
			user = GetUserResponse(
				id = "user-id",
				cid = 12345678,
				usbId = "12-34567",
				email = "12-34567@usb.ve",
				fullName = "Ada Lovelace",
				firstNames = "Ada",
				lastNames = "Lovelace",
				pictureUrl = "",
				careerName = "Ingenieria",
				careerCode = 12039,
				scholarship = false,
				grade = 4.5,
				enrolledSubjects = 0,
				enrolledCredits = 0,
				approvedSubjects = 0,
				approvedCredits = 0,
				approvedRelation = 0.0,
				retiredSubjects = 0,
				retiredCredits = 0,
				retiredRelation = 0.0,
				failedSubjects = 0,
				failedCredits = 0,
				failedRelation = 0.0,
				lastUpdate = 123L
			),
			sync = sync
		)
	}

	private fun syncReportResponse(
		status: SyncReportStatusResponse,
		record: SyncSourceStatusResponse,
		enrollment: SyncSourceStatusResponse
	): SyncReportResponse {
		return SyncReportResponse(
			status = status,
			sources = SyncReportSourcesResponse(
				record = SyncSourceReportResponse(record),
				enrollment = SyncSourceReportResponse(enrollment)
			)
		)
	}
}
