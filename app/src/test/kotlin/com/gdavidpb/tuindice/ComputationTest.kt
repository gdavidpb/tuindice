package com.gdavidpb.tuindice

import com.gdavidpb.tuindice.base.domain.model.Quarter
import com.gdavidpb.tuindice.base.domain.model.Subject
import com.gdavidpb.tuindice.base.utils.extension.*
import com.gdavidpb.tuindice.evaluations.utils.extension.toSubjectGrade
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.*
import kotlin.math.floor

class ComputationTest {

	object TestValues {
		const val STATUS_QUARTER_CURRENT = 0
		const val STATUS_QUARTER_COMPLETED = 1
		const val STATUS_QUARTER_RETIRED = 3

		const val STATUS_SUBJECT_OK = 0
		const val STATUS_SUBJECT_RETIRED = 1

		val DEFAULT_LOCALE: Locale = Locale("es", "VE")
		val DEFAULT_TIME_ZONE: TimeZone = TimeZone.getTimeZone("America/Caracas")
	}

	@Before
	fun setDefaultLocaleTimeZone() {
		Locale.setDefault(TestValues.DEFAULT_LOCALE)
		TimeZone.setDefault(TestValues.DEFAULT_TIME_ZONE)
	}

	@Test
	fun toGrade_Test() {
		(0 until 30)
			.all { it.toDouble().toSubjectGrade() == 1 }
			.also(::assertTrue)

		(30 until 50)
			.all { it.toDouble().toSubjectGrade() == 2 }
			.also(::assertTrue)

		(50 until 70)
			.all { it.toDouble().toSubjectGrade() == 3 }
			.also(::assertTrue)

		(70 until 85)
			.all { it.toDouble().toSubjectGrade() == 4 }
			.also(::assertTrue)

		(85..100)
			.all { it.toDouble().toSubjectGrade() == 5 }
			.also(::assertTrue)
	}

	@Test
	fun filterNoEffect_Test() {
		val actualCase1 = listOf(
			createSubject(code = "MA1111", grade = 5),
			createSubject(code = "MA1111", grade = 2),
			createSubject(code = "MA1111", grade = 1)
		).removeNoEffect()

		val expectedCase1 = listOf(
			createSubject(code = "MA1111", grade = 5),
			createSubject(code = "MA1111", grade = 1)
		)

		val actualCase2 = listOf(
			createSubject(code = "MA1111", grade = 5),
			createSubject(code = "MA1111", grade = 1)
		).removeNoEffect()

		val expectedCase2 = listOf(
			createSubject(code = "MA1111", grade = 5)
		)

		val actualCase3 = listOf(
			createSubject(code = "MA1111", grade = 5)
		).removeNoEffect()

		val expectedCase3 = listOf(
			createSubject(code = "MA1111", grade = 5)
		)

		assertEquals(expectedCase1, actualCase1)
		assertEquals(expectedCase2, actualCase2)
		assertEquals(expectedCase3, actualCase3)
	}

	@Test
	fun computeCredits_Test() {
		val subjects = listOf(
			createSubject(grade = 5, credits = 4),
			createSubject(grade = 0, credits = 3),
			createSubject(grade = 2, credits = 2)
		)

		val actualCredits = subjects.computeCredits()
		val expectedCredits = 4 + 0 + 2

		assertEquals(expectedCredits, actualCredits)
	}

	@Test
	fun computeGrade_Test() {
		val subjects = listOf(
			createSubject(grade = 5, credits = 4),
			createSubject(grade = 0, credits = 3),
			createSubject(grade = 2, credits = 2)
		)

		val creditsSum = (4 + 0 + 2).toDouble()
		val weightedSum = ((5 * 4) + (0 * 3) + (2 * 2)).toDouble()

		val actualGrade = subjects.computeGrade()
		val expectedGrade = floor(weightedSum / creditsSum * 10000.0) / 10000.0

		assertEquals(expectedGrade, actualGrade, 0.0)
	}

	@Test
	fun computeGradeSum_Test() {
		val quarter1 = createQuarter(
			startDate = "Enero 2019".parse("MMMM yyyy")!!,
			endDate = "Marzo 2019".parse("MMMM yyyy")!!,
			status = TestValues.STATUS_QUARTER_COMPLETED,
			subjects = mutableListOf(
				createSubject(code = "MA1111", grade = 2, credits = 4),
				createSubject(code = "ID1111", grade = 3, credits = 3),
				createSubject(code = "CSA211", grade = 3, credits = 3)
			)
		)

		val quarter2 = createQuarter(
			startDate = "Julio 2019".parse("MMMM yyyy")!!,
			endDate = "Agosto 2019".parse("MMMM yyyy")!!,
			status = TestValues.STATUS_QUARTER_COMPLETED,
			subjects = mutableListOf(
				createSubject(code = "MA1111", grade = 2, credits = 4)
			)
		)

		val quarter3 = createQuarter(
			startDate = "Septiembre 2019".parse("MMMM yyyy")!!,
			endDate = "Diciembre 2019".parse("MMMM yyyy")!!,
			status = TestValues.STATUS_QUARTER_COMPLETED,
			subjects = mutableListOf(
				createSubject(code = "MA1111", grade = 5, credits = 4),
				createSubject(code = "ID1112", grade = 5, credits = 3)
			)
		)

		val quarter4 = createQuarter(
			startDate = "Enero 2020".parse("MMMM yyyy")!!,
			endDate = "Marzo 2020".parse("MMMM yyyy")!!,
			status = TestValues.STATUS_QUARTER_RETIRED,
			subjects = mutableListOf(
				createSubject(code = "MA1112", grade = 5, credits = 4),
				createSubject(code = "ID1113", grade = 5, credits = 3)
			)
		)

		val quarter5 = createQuarter(
			startDate = "Septiembre 2020".parse("MMMM yyyy")!!,
			endDate = "Diciembre 2020".parse("MMMM yyyy")!!,
			status = TestValues.STATUS_QUARTER_CURRENT,
			subjects = mutableListOf(
				createSubject(code = "MA1112", grade = 5, credits = 4),
				createSubject(
					code = "ID1113",
					grade = 5,
					credits = 3,
					status = TestValues.STATUS_SUBJECT_RETIRED
				)
			)
		)

		val quarters = listOf(quarter5, quarter4, quarter3, quarter2, quarter1)

		val creditsSum = (4 + 3 + 3 + 4 + 3 + 4).toDouble()
		val weightedSum = ((2 * 4) + (3 * 3) + (3 * 3) + (5 * 4) + (5 * 3) + (5 * 4)).toDouble()

		val actualGradeSum = quarters.computeGradeSum()
		val expectedGradeSum = floor(weightedSum / creditsSum * 10000.0) / 10000.0

		assertEquals(expectedGradeSum, actualGradeSum, 0.0)
	}

	private fun createQuarter(
		startDate: Date = Date(),
		endDate: Date = Date(),
		status: Int = 0,
		subjects: MutableList<Subject> = mutableListOf()
	) = Quarter(
		id = "",
		name = (startDate to endDate).formatQuarterName(),
		startDate = startDate,
		endDate = endDate,
		grade = 0.0,
		gradeSum = 0.0,
		credits = 0,
		status = status,
		subjects = subjects
	)

	private fun createSubject(
		code: String = "",
		name: String = "",
		grade: Int = 0,
		credits: Int = 0,
		status: Int = TestValues.STATUS_SUBJECT_OK
	) = Subject(
		id = "",
		qid = "",
		code = code,
		name = name,
		credits = credits,
		grade = grade,
		status = status
	)
}