package com.gdavidpb.tuindice.academiccore.domain.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SubjectGradeScaleTest {
	@Test
	fun pointsEarnTheirGradeAtEachThreshold() {
		assertEquals(1, SubjectGradeScale.gradeOf(0.0))
		assertEquals(1, SubjectGradeScale.gradeOf(29.4))
		assertEquals(2, SubjectGradeScale.gradeOf(29.5))
		assertEquals(2, SubjectGradeScale.gradeOf(49.4))
		assertEquals(3, SubjectGradeScale.gradeOf(49.5))
		assertEquals(3, SubjectGradeScale.gradeOf(69.4))
		assertEquals(4, SubjectGradeScale.gradeOf(69.5))
		assertEquals(4, SubjectGradeScale.gradeOf(84.4))
		assertEquals(5, SubjectGradeScale.gradeOf(84.5))
		assertEquals(5, SubjectGradeScale.gradeOf(100.0))
	}

	@Test
	fun pointsBelowZeroAreAGradeOne() {
		assertEquals(1, SubjectGradeScale.gradeOf(-0.4))
		assertEquals(1, SubjectGradeScale.gradeOf(-30.0))
	}

	@Test
	fun minimumPointsAreTheLowestPointsOfEachGrade() {
		assertEquals(0.0, SubjectGradeScale.minimumPointsFor(1))
		assertEquals(29.5, SubjectGradeScale.minimumPointsFor(2))
		assertEquals(49.5, SubjectGradeScale.minimumPointsFor(3))
		assertEquals(69.5, SubjectGradeScale.minimumPointsFor(4))
		assertEquals(84.5, SubjectGradeScale.minimumPointsFor(5))
	}

	@Test
	fun theMinimumOfEachGradeEarnsItAndJustBelowItEarnsTheGradeBefore() {
		(2..5).forEach { grade ->
			val minimum = SubjectGradeScale.minimumPointsFor(grade)

			assertEquals(grade, SubjectGradeScale.gradeOf(minimum))
			assertEquals(grade - 1, SubjectGradeScale.gradeOf(minimum - 0.1))
		}
	}

	@Test
	fun aGradeOutsideTheScaleHasNoMinimumPoints() {
		assertFailsWith<IllegalArgumentException> { SubjectGradeScale.minimumPointsFor(0) }
		assertFailsWith<IllegalArgumentException> { SubjectGradeScale.minimumPointsFor(6) }
	}
}
