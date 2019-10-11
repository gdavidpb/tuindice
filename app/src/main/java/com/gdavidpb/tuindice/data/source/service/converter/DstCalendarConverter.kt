package com.gdavidpb.tuindice.data.source.service.converter

import com.gdavidpb.tuindice.data.model.service.DstQuarterCalendar
import org.jsoup.nodes.Element
import pl.droidsonroids.jspoon.ElementConverter
import pl.droidsonroids.jspoon.annotation.Selector

open class DstCalendarConverter : ElementConverter<DstQuarterCalendar> {

    /*
    private val year by lazy {
        Calendar.getInstance().get(Calendar.YEAR)
    }
    */

    override fun convert(node: Element, selector: Selector): DstQuarterCalendar {
        /*
        val values = node
                .select("td + td")
                .map { it.text() }

        val (startDate, endDate) = values[0]
                .split("-")
                .map {
                    "$it $year"
                            .trim()
                            .toLowerCase()
                            .normalize()
                            .parse("dd 'de' MMM yyyy")
                }

        val (correctionDate, giveUpDeadline, degreeRequestDeadline) = values
                .subList(1, 4)
                .map {
                    "$it $year"
                            .toLowerCase()
                            .normalize()
                            .parse("dd 'de' MMM yyyy")
                }

        val (graduationStartDate, graduationEndDate) = if (values[4] != "-")
            values[4]
                    .split(",")
                    .map {
                        "$it/$year"
                                .parse("dd/MM/yyyy")
                    }
        else
            listOf(null, null)

        val (documentsRequestDeadline, nextEnrollmentDate, minutesDeliveryDeadline) = values
                .subList(5, 8)
                .map {
                    val sub = it.substringBefore("-").trim() //todo improve

                    "$sub $year"
                            .toLowerCase()
                            .normalize()
                            .parse("dd 'de' MMM yyyy")
                }

        return DstQuarterCalendar(
                startDate = startDate,
                endDate = endDate,
                correctionDate = correctionDate,
                giveUpDeadline = giveUpDeadline,
                degreeRequestDeadline = degreeRequestDeadline,
                graduationStartDate = graduationStartDate,
                graduationEndDate = graduationEndDate,
                documentsRequestDeadline = documentsRequestDeadline,
                nextEnrollmentDate = nextEnrollmentDate,
                minutesDeliveryDeadline = minutesDeliveryDeadline
        )
        */

        return DstQuarterCalendar(
                startDate = null,
                endDate = null,
                correctionDate = null,
                giveUpDeadline = null,
                degreeRequestDeadline = null,
                graduationStartDate = null,
                graduationEndDate = null,
                documentsRequestDeadline = null,
                nextEnrollmentDate = null,
                minutesDeliveryDeadline = null
        )
    }

    /* Workaround: for unknown reason september is abbreviated by Android as "sept" */
    private fun String.normalize(): String = replace("sep".toRegex(), "sept")

    private inline fun <reified T> Iterable<T>.ensuresSize(size: Int): List<T?> = when {
        count() == size -> toList()
        count() > size -> take(size)
        count() < size -> union(arrayOfNulls<T>(size - count()).toList()).toList()
        else -> throw IllegalStateException()
    }
}