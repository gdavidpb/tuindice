package com.gdavidpb.tuindice.domain.model

import com.gdavidpb.tuindice.data.utils.DEFAULT_LOCALE
import com.gdavidpb.tuindice.data.utils.QUARTER_ENDS
import com.gdavidpb.tuindice.data.utils.QUARTER_STARTS
import com.gdavidpb.tuindice.data.utils.format
import java.util.*

data class Period(val startDate: Date?,
                  val endDate: Date?) {

    //todo check .time
    private fun move(n: Int): Period {
        return copy(
                startDate = Calendar.getInstance(DEFAULT_LOCALE).apply {
                    time = startDate

                    do {
                        add(Calendar.MONTH, n)
                    } while (QUARTER_STARTS.indexOf(get(Calendar.MONTH)) == -1)
                }.time,
                endDate = Calendar.getInstance(DEFAULT_LOCALE).apply {
                    time = endDate

                    do {
                        add(Calendar.MONTH, n)
                    } while (QUARTER_ENDS.indexOf(get(Calendar.MONTH)) == -1)
                }.time)
    }

    operator fun inc(): Period {
        move(1)
        return this
    }

    operator fun dec(): Period {
        move(-1)
        return this
    }

    operator fun compareTo(period: Period): Int {
        val start = startDate?.compareTo(period.startDate) ?: -1

        if (start != 0) return start

        val end = endDate?.compareTo(period.endDate) ?: -1

        if (end != 0) return end

        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other !is Period) return false

        return startDate == other.startDate &&
                endDate == other.endDate
    }

    override fun toString(): String {
        val start = startDate?.format("MMM")?.capitalize()
        val end = endDate?.format("MMM")?.capitalize()
        val year = startDate?.format("yyyy")

        return "$start - $end $year".replace("\\.".toRegex(), "")
    }

    override fun hashCode(): Int {
        return startDate.hashCode() or endDate.hashCode()
    }
}