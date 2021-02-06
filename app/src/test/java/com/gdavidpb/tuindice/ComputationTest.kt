package com.gdavidpb.tuindice

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.utils.STATUS_QUARTER_COMPLETED
import com.gdavidpb.tuindice.utils.STATUS_QUARTER_CURRENT
import com.gdavidpb.tuindice.utils.STATUS_QUARTER_RETIRED
import com.gdavidpb.tuindice.utils.STATUS_SUBJECT_RETIRED
import com.gdavidpb.tuindice.utils.extensions.*
import org.junit.Assert.*
import org.junit.Test
import java.util.*
import kotlin.math.floor

class ComputationTest {
    @Test
    fun toGrade_Test() {
        (0 until 30)
                .all { it.toDouble().toGrade() == 1 }
                .also(::assertTrue)

        (30 until 50)
                .all { it.toDouble().toGrade() == 2 }
                .also(::assertTrue)

        (50 until 70)
                .all { it.toDouble().toGrade() == 3 }
                .also(::assertTrue)

        (70 until 85)
                .all { it.toDouble().toGrade() == 4 }
                .also(::assertTrue)

        (85..100)
                .all { it.toDouble().toGrade() == 5 }
                .also(::assertTrue)
    }

    @Test
    fun filterNoEffect_Test() {
        val actualSubjectsContainsNoEffect = listOf(
                createSubject(code = "MA1111", grade = 5),
                createSubject(code = "MA1111", grade = 2),
                createSubject(code = "MA1111", grade = 1)
        ).filterNoEffect()

        val expectedSubjectsContainsNoEffect = listOf(
                createSubject(code = "MA1111", grade = 5),
                createSubject(code = "MA1111", grade = 1)
        )

        val actualSubjectNoContainsNoEffect = listOf(
                createSubject(code = "MA1111", grade = 5)
        ).filterNoEffect()

        val expectedSubjectNoContainsNoEffect = listOf(
                createSubject(code = "MA1111", grade = 5)
        ).filterNoEffect()

        assertEquals(expectedSubjectsContainsNoEffect, actualSubjectsContainsNoEffect)
        assertEquals(expectedSubjectNoContainsNoEffect, actualSubjectNoContainsNoEffect)
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
    fun isUsbId_Test() {
        assertTrue("11-11111".isUsbId())
        assertTrue("00-00000".isUsbId())

        assertFalse("00000000".isUsbId())
        assertFalse("00-qwert".isUsbId())
        assertFalse("qw-00000".isUsbId())
        assertFalse("0-0".isUsbId())
        assertFalse("a-b".isUsbId())
        assertFalse("0".isUsbId())
        assertFalse("qwerty".isUsbId())
        assertFalse("     ".isUsbId())
        assertFalse("".isUsbId())
    }

    @Test
    fun isUpdated_Test() {
        val today = Date()
        val yesterday = Calendar.getInstance().run {
            add(Calendar.DATE, -1)

            time
        }

        val updatedAccount = createAccount(lastUpdate = today)
        val outdatedAccount = createAccount(lastUpdate = yesterday)

        val actualTrue = updatedAccount.isUpdated()
        val actualFalse = outdatedAccount.isUpdated()

        assertTrue(actualTrue)
        assertFalse(actualFalse)
    }

    @Test
    fun computeGradeSum_Test() {
        val quarter1 = createQuarter(
                startDate = "Enero 2019".parse("MMMM yyyy")!!,
                endDate = "Marzo 2019".parse("MMMM yyyy")!!,
                status = STATUS_QUARTER_COMPLETED,
                subjects = mutableListOf(
                        createSubject(code = "MA1111", grade = 2, credits = 4),
                        createSubject(code = "ID1111", grade = 3, credits = 3),
                        createSubject(code = "CSA211", grade = 3, credits = 3)
                )
        )

        val quarter2 = createQuarter(
                startDate = "Julio 2019".parse("MMMM yyyy")!!,
                endDate = "Agosto 2019".parse("MMMM yyyy")!!,
                status = STATUS_QUARTER_COMPLETED,
                subjects = mutableListOf(
                        createSubject(code = "MA1111", grade = 2, credits = 4)
                )
        )

        val quarter3 = createQuarter(
                startDate = "Septiembre 2019".parse("MMMM yyyy")!!,
                endDate = "Diciembre 2019".parse("MMMM yyyy")!!,
                status = STATUS_QUARTER_COMPLETED,
                subjects = mutableListOf(
                        createSubject(code = "MA1111", grade = 5, credits = 4),
                        createSubject(code = "ID1112", grade = 5, credits = 3)
                )
        )

        val quarter4 = createQuarter(
                startDate = "Enero 2020".parse("MMMM yyyy")!!,
                endDate = "Marzo 2020".parse("MMMM yyyy")!!,
                status = STATUS_QUARTER_RETIRED,
                subjects = mutableListOf(
                        createSubject(code = "MA1112", grade = 5, credits = 4),
                        createSubject(code = "ID1113", grade = 5, credits = 3)
                )
        )

        val quarter5 = createQuarter(
                startDate = "Septiembre 2020".parse("MMMM yyyy")!!,
                endDate = "Diciembre 2020".parse("MMMM yyyy")!!,
                status = STATUS_QUARTER_CURRENT,
                subjects = mutableListOf(
                        createSubject(code = "MA1112", grade = 5, credits = 4),
                        createSubject(code = "ID1113", grade = 5, credits = 3, status = STATUS_SUBJECT_RETIRED)
                )
        )

        val quarters = listOf(quarter5, quarter4, quarter3, quarter2, quarter1)

        val creditsSum = (4 + 3 + 3 + 4 + 3 + 4).toDouble()
        val weightedSum = ((2 * 4) + (3 * 3) + (3 * 3) + (5 * 4) + (5 * 3) + (5 * 4)).toDouble()

        val actualGradeSum = quarters.computeGradeSum()
        val expectedGradeSum = floor(weightedSum / creditsSum * 10000.0) / 10000.0

        assertEquals(expectedGradeSum, actualGradeSum, 0.0)
    }

    private fun createAccount(
            lastUpdate: Date = Date()
    ) = Account(
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            0,
            false,
            0.0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            0,
            lastUpdate,
            0
    )

    private fun createQuarter(
            startDate: Date = Date(),
            endDate: Date = Date(),
            status: Int = 0,
            subjects: MutableList<Subject> = mutableListOf()
    ) = Quarter(
            "",
            startDate,
            endDate,
            0.0,
            0.0,
            0,
            status,
            subjects
    )

    private fun createSubject(
            code: String = "",
            name: String = "",
            grade: Int = 0,
            credits: Int = 0,
            status: Int = 0
    ) = Subject(
            "",
            "",
            code,
            name,
            credits,
            grade,
            status
    )

    private fun String.isUsbId() = matches("^\\d{2}-\\d{5}$".toRegex())
}