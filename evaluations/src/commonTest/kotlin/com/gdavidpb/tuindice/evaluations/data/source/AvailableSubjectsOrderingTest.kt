package com.gdavidpb.tuindice.evaluations.data.source

import com.gdavidpb.tuindice.evaluations.data.model.LocalEditableAttemptDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals

class AvailableSubjectsOrderingTest {
	@Test
	fun sortedAvailableSubjectsByCode_ordersSubjectsAscendingByCode() {
		val subjects = listOf(
			localSubject(id = "subject-1", code = "MAT2205"),
			localSubject(id = "subject-2", code = "BIO0140"),
			localSubject(id = "subject-3", code = "QUI1000")
		)

		val sortedSubjects = subjects.sortedBy(LocalEditableAttemptDescriptor::code)

		assertEquals(
			listOf("BIO0140", "MAT2205", "QUI1000"),
			sortedSubjects.map(LocalEditableAttemptDescriptor::code)
		)
	}
}

private fun localSubject(
	id: String,
	code: String
) = LocalEditableAttemptDescriptor(
	id = id,
	termId = "quarter-1",
	code = code,
	name = code,
	credits = 10,
	grade = 70
)
