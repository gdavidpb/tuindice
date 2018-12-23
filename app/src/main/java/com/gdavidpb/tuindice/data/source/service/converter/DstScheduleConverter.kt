package com.gdavidpb.tuindice.data.source.service.converter

import com.gdavidpb.tuindice.data.model.service.DstScheduleEntry
import com.gdavidpb.tuindice.data.model.service.DstScheduledSubject
import org.jsoup.nodes.Element
import pl.droidsonroids.jspoon.ElementConverter
import pl.droidsonroids.jspoon.annotation.Selector

class DstScheduleConverter : ElementConverter<List<DstScheduledSubject>> {
    override fun convert(node: Element, selector: Selector): List<DstScheduledSubject> {
        return node
                .select("td")
                .map { it.text() }
                .chunked(11)
                .map {
                    val code = it[0]
                            .replace("[^A-Z0-9]".toRegex(), "")

                    val name = it[10]
                            .replace("^[^\\s]+".toRegex(), "")
                            .trim()

                    val classroom = it[3]

                    val (section, credits) = it
                            .subList(1, 3)
                            .map { value -> value.toInt() }

                    val schedule = it
                            .subList(4, 10)
                            .mapIndexedNotNull { index, s ->
                                if (s.isNotEmpty()) {
                                    /* Calendar dayOfWeek transformation */
                                    val dayOfWeek = index + 2
                                    val (startAt, endAt) = s
                                            .split("-")
                                            .map { value -> value.toInt() }

                                    DstScheduleEntry(dayOfWeek, startAt, endAt)
                                } else
                                    null
                            }

                    DstScheduledSubject(
                            code = code,
                            section = section,
                            name = name,
                            credits = credits,
                            classroom = classroom,
                            schedule = schedule
                    )
                }
    }
}