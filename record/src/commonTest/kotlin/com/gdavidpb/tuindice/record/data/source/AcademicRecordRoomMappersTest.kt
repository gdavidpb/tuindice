package com.gdavidpb.tuindice.record.data.source

import com.gdavidpb.tuindice.academiccore.domain.model.ProjectionViewMode
import com.gdavidpb.tuindice.academiccore.domain.model.TermKind
import com.gdavidpb.tuindice.academiccore.domain.model.TermProjection
import com.gdavidpb.tuindice.academiccore.domain.model.AcademicTerm
import kotlin.test.Test
import kotlin.test.assertEquals

class AcademicRecordRoomMappersTest {
	@Test
	fun academicTerm_roundTripsThroughRoomEntity_withTermKind() {
		val term = AcademicTerm(
			id = "term-1",
			label = "Enero - Marzo 2026",
			startAtMillis = 1_000L,
			endAtMillis = 2_000L,
			order = 0,
			kind = TermKind.OFFICIAL_CURRENT
		)

		val roundTrip = listOf(term.toAcademicTermEntity(recordId = "self"))
			.toAcademicTerms(attempts = emptyList())
			.single()

		assertEquals(TermKind.OFFICIAL_CURRENT, roundTrip.kind)
	}

	@Test
	fun termProjection_roundTripsThroughRoomEntity_withTermKind() {
		val term = TermProjection(
			id = "term-1",
			label = "Synthetic Term",
			startAtMillis = 1_000L,
			endAtMillis = 2_000L,
			order = 1,
			kind = TermKind.SYNTHETIC,
			grade = 4.5,
			gradeSum = 4.2,
			credits = 8,
			creditsSum = 42,
			attempts = emptyList()
		)

		val roundTrip = listOf(
			term.toAcademicTermProjectionEntity(
				recordId = "self",
				viewMode = ProjectionViewMode.SIMULATION
			)
		).toTermProjections(attempts = emptyList()).single()

		assertEquals(TermKind.SYNTHETIC, roundTrip.kind)
	}
}
