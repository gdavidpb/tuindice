package com.gdavidpb.tuindice.academiccore.domain.engine

import kotlin.math.roundToInt

/**
 * The official scale that turns the points of a subject into its grade, from 1 to 5. The points are rounded to the
 * nearest whole number first, so half a point rounds up: 29.5 is a 2 and 29.4 is a 1.
 */
object SubjectGradeScale {
	private const val GRADE_1 = 1
	private const val GRADE_2 = 2
	private const val GRADE_3 = 3
	private const val GRADE_4 = 4
	private const val GRADE_5 = 5

	// The first whole score that earns each grade from 2 up. A lower whole score earns a 1.
	private const val GRADE_2_FROM = 30
	private const val GRADE_3_FROM = 50
	private const val GRADE_4_FROM = 70
	private const val GRADE_5_FROM = 85

	private const val HALF_POINT = 0.5

	/** The grade earned with [points]: a whole score below 30 is a 1, and each grade starts at its own threshold. */
	fun gradeOf(points: Double): Int {
		val wholePoints = points.roundToInt()

		return when {
			wholePoints >= GRADE_5_FROM -> GRADE_5
			wholePoints >= GRADE_4_FROM -> GRADE_4
			wholePoints >= GRADE_3_FROM -> GRADE_3
			wholePoints >= GRADE_2_FROM -> GRADE_2
			else -> GRADE_1
		}
	}

	/**
	 * The fewest points that earn [grade]: 0.0 for a 1, and for the others the half point below the first whole score
	 * of the grade, so that rounding lands on it. Throws for a grade outside the scale.
	 */
	fun minimumPointsFor(grade: Int): Double = when (grade) {
		GRADE_1 -> 0.0
		GRADE_2 -> GRADE_2_FROM - HALF_POINT
		GRADE_3 -> GRADE_3_FROM - HALF_POINT
		GRADE_4 -> GRADE_4_FROM - HALF_POINT
		GRADE_5 -> GRADE_5_FROM - HALF_POINT
		else -> throw IllegalArgumentException("There is no subject grade $grade")
	}
}
