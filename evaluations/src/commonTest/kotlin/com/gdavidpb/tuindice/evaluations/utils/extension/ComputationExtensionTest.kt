package com.gdavidpb.tuindice.evaluations.utils.extension

import kotlin.test.Test
import kotlin.test.assertTrue

class ComputationExtensionTest {
	@Test
	fun toSubjectGrade_mapsPercentageRangesToSubjectGrades() {
		assertTrue((0 until 30).all { value -> value.toDouble().toSubjectGrade() == 1 })
		assertTrue((30 until 50).all { value -> value.toDouble().toSubjectGrade() == 2 })
		assertTrue((50 until 70).all { value -> value.toDouble().toSubjectGrade() == 3 })
		assertTrue((70 until 85).all { value -> value.toDouble().toSubjectGrade() == 4 })
		assertTrue((85..100).all { value -> value.toDouble().toSubjectGrade() == 5 })
	}
}
