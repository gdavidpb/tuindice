package com.gdavidpb.tuindice.record.presentation.mapper

import com.gdavidpb.tuindice.record.domain.model.RecordRejectionKind
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

// Resolves compose resources, so like the other UiTests it only runs on iOS.
class RecordRejectionMessageUiTest {
	@Test
	fun when_aGradeIsRejected_then_itSaysTheGradeCouldNotBeSavedWhateverTheCount() = runTest {
		val expected = "No pudimos guardar tu nota. Revisa el valor e inténtalo de nuevo."

		assertEquals(expected, recordRejectionMessage(kind = RecordRejectionKind.Grade, count = 1))
		assertEquals(expected, recordRejectionMessage(kind = RecordRejectionKind.Grade, count = 3))
	}

	@Test
	fun when_aTermIsRejected_then_theMessageStaysTheTermOneAndCountsPlurals() = runTest {
		assertEquals(
			"El servidor rechazó un cambio del trimestre y lo descartamos",
			recordRejectionMessage(kind = RecordRejectionKind.Term, count = 1)
		)
		assertEquals(
			"El servidor rechazó 2 cambios del trimestre y los descartamos",
			recordRejectionMessage(kind = RecordRejectionKind.Term, count = 2)
		)
	}
}
