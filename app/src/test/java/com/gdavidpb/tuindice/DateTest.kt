package com.gdavidpb.tuindice

import com.gdavidpb.tuindice.utils.extensions.*
import org.junit.Assert.*
import org.junit.Test
import java.util.*

class DateTest {
    @Test
    fun isToday_Test() {
        assertTrue(today().isToday())
        assertTrue(todayStart().isToday())
        assertTrue(todayEnd().isToday())

        assertFalse(tomorrow().isToday())
        assertFalse(tomorrowStart().isToday())
        assertFalse(tomorrowEnd().isToday())
        assertFalse(yesterday().isToday())
        assertFalse(yesterdayStart().isToday())
        assertFalse(yesterdayEnd().isToday())
    }

    @Test
    fun isTomorrow_Test() {
        assertTrue(tomorrow().isTomorrow())
        assertTrue(tomorrowStart().isTomorrow())
        assertTrue(tomorrowEnd().isTomorrow())

        assertFalse(today().isTomorrow())
        assertFalse(todayStart().isTomorrow())
        assertFalse(todayEnd().isTomorrow())
        assertFalse(yesterday().isTomorrow())
        assertFalse(yesterdayStart().isTomorrow())
        assertFalse(yesterdayEnd().isTomorrow())
    }

    @Test
    fun isYesterday_Test() {
        assertTrue(yesterday().isYesterday())
        assertTrue(yesterdayStart().isYesterday())
        assertTrue(yesterdayEnd().isYesterday())

        assertFalse(today().isYesterday())
        assertFalse(todayStart().isYesterday())
        assertFalse(todayEnd().isYesterday())
        assertFalse(tomorrow().isYesterday())
        assertFalse(tomorrowStart().isYesterday())
        assertFalse(tomorrowEnd().isYesterday())
    }

    @Test
    fun isThisWeek_Test() {
        val expectedForYesterday = todayDayOfWeek() != Calendar.MONDAY
        val actualForYesterday = yesterday().isThisWeek()

        val expectedForTomorrow = todayDayOfWeek() != Calendar.SUNDAY
        val actualForTomorrow = tomorrow().isThisWeek()

        assertTrue(today().isThisWeek())
        assertTrue(todayStart().isThisWeek())
        assertTrue(todayEnd().isThisWeek())
        assertTrue(thisWeekStart().isThisWeek())
        assertTrue(thisWeekEnd().isThisWeek())

        assertFalse(nextWeekStart().isThisWeek())
        assertFalse(nextWeekEnd().isThisWeek())

        assertEquals(expectedForYesterday, actualForYesterday)
        assertEquals(expectedForTomorrow, actualForTomorrow)
    }

    @Test
    fun isNextWeek_Test() {
        assertTrue(nextWeekStart().isNextWeek())
        assertTrue(nextWeekEnd().isNextWeek())

        assertFalse(today().isNextWeek())
        assertFalse(todayStart().isNextWeek())
        assertFalse(todayEnd().isNextWeek())
        assertFalse(thisWeekStart().isNextWeek())
        assertFalse(thisWeekEnd().isNextWeek())
    }

    private fun today(): Date = Date()

    private fun todayDayOfWeek(): Int = Calendar.getInstance().run {
        get(Calendar.DAY_OF_WEEK)
    }

    private fun thisWeekStart(): Date = Calendar.getInstance().run {
        restartDay()

        while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
            add(Calendar.DATE, -1)

        time
    }

    private fun thisWeekEnd(): Date = Calendar.getInstance().run {
        restartDay()

        while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
            add(Calendar.DATE, 1)

        add(Calendar.DATE, 1)
        add(Calendar.MILLISECOND, -1)

        time
    }

    private fun nextWeekStart(): Date = Calendar.getInstance().run {
        restartDay()

        add(Calendar.WEEK_OF_YEAR, 1)

        while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY)
            add(Calendar.DATE, -1)

        time
    }

    private fun nextWeekEnd(): Date = Calendar.getInstance().run {
        restartDay()

        add(Calendar.WEEK_OF_YEAR, 1)

        while (get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)
            add(Calendar.DATE, 1)

        add(Calendar.DATE, 1)
        add(Calendar.MILLISECOND, -1)

        time
    }

    private fun tomorrow(): Date = Calendar.getInstance().run {
        add(Calendar.DATE, 1)

        time
    }

    private fun yesterday(): Date = Calendar.getInstance().run {
        add(Calendar.DATE, -1)

        time
    }

    private fun todayStart(): Date = Calendar.getInstance().run {
        restartDay()

        time
    }

    private fun todayEnd(): Date = Calendar.getInstance().run {
        restartDay()

        add(Calendar.DATE, 1)
        add(Calendar.MILLISECOND, -1)

        time
    }

    private fun tomorrowStart(): Date = Calendar.getInstance().run {
        restartDay()

        add(Calendar.DATE, 1)

        time
    }

    private fun tomorrowEnd(): Date = Calendar.getInstance().run {
        restartDay()

        add(Calendar.DATE, 2)
        add(Calendar.MILLISECOND, -1)

        time
    }

    private fun yesterdayStart(): Date = Calendar.getInstance().run {
        restartDay()

        add(Calendar.DATE, -1)

        time
    }

    private fun yesterdayEnd(): Date = Calendar.getInstance().run {
        add(Calendar.DATE, -1)

        time
    }

    private fun Calendar.restartDay() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
}