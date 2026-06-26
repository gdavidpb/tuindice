package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubject
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailability
import com.gdavidpb.tuindice.record.domain.model.SyntheticTermSubjectAvailabilityDetail
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CreateTermSubjectItemTest {
	@Test
	fun toCreateTermSubjectItem_uppercasesName_andKeepsSubjectReference() {
		val subject = syntheticSubject(name = "Matemáticas vi")

		val item = subject.toCreateTermSubjectItem()

		assertEquals("MATEMÁTICAS VI", item.nameText)
		assertEquals(subject, item.subject)
		assertEquals("MA1112", item.subjectCode)
	}

	@Test
	fun creditsText_formatsCreditsWithUnit() {
		assertEquals("3 UC", syntheticSubject(credits = 3).toCreateTermSubjectItem().creditsText)
		assertEquals("0 UC", syntheticSubject(credits = 0).toCreateTermSubjectItem().creditsText)
	}

	@Test
	fun canAdd_isTrue_forAvailableNotInPensumAndBlockedSubjects() {
		assertTrue(
			syntheticSubject(availability = SyntheticTermSubjectAvailability.AVAILABLE)
				.toCreateTermSubjectItem().canAdd
		)
		assertTrue(
			syntheticSubject(availability = SyntheticTermSubjectAvailability.NOT_IN_PENSUM)
				.toCreateTermSubjectItem().canAdd
		)
		assertTrue(
			syntheticSubject(availability = SyntheticTermSubjectAvailability.BLOCKED)
				.toCreateTermSubjectItem().canAdd
		)
	}

	@Test
	fun canAdd_isFalse_forApprovedCurrentOrPlannedSubjects() {
		assertFalse(
			syntheticSubject(availability = SyntheticTermSubjectAvailability.APPROVED)
				.toCreateTermSubjectItem().canAdd
		)
		assertFalse(
			syntheticSubject(availability = SyntheticTermSubjectAvailability.CURRENT)
				.toCreateTermSubjectItem().canAdd
		)
		assertFalse(
			syntheticSubject(availability = SyntheticTermSubjectAvailability.ALREADY_PLANNED)
				.toCreateTermSubjectItem().canAdd
		)
	}

	@Test
	fun availabilityDetail_isExposedThroughItem() {
		val detail = SyntheticTermSubjectAvailabilityDetail(
			termLabel = "Sep - Dic 2027",
			missingSubjectCodes = listOf("MA1111")
		)
		val item = syntheticSubject(
			availability = SyntheticTermSubjectAvailability.BLOCKED,
			availabilityDetail = detail
		).toCreateTermSubjectItem()

		assertEquals(SyntheticTermSubjectAvailability.BLOCKED, item.availability)
		assertEquals(detail, item.availabilityDetail)
	}

	private fun syntheticSubject(
		name: String = "Matemáticas VI",
		credits: Int = 4,
		availability: SyntheticTermSubjectAvailability = SyntheticTermSubjectAvailability.AVAILABLE,
		availabilityDetail: SyntheticTermSubjectAvailabilityDetail? = null
	): SyntheticTermSubject {
		return SyntheticTermSubject(
			subjectCode = "MA1112",
			name = name,
			credits = credits,
			availability = availability,
			availabilityDetail = availabilityDetail
		)
	}
}
