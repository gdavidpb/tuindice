package com.gdavidpb.tuindice.data.source.service.converter

import com.gdavidpb.tuindice.data.model.service.DstScheduleEntry
import com.gdavidpb.tuindice.data.model.service.DstScheduledSubject
import org.jsoup.nodes.Element
import pl.droidsonroids.jspoon.ElementConverter
import pl.droidsonroids.jspoon.annotation.Selector

open class DstScheduleConverter : ElementConverter<List<DstScheduledSubject>> {
    override fun convert(node: Element, selector: Selector): List<DstScheduledSubject> {
        return node
                .select("td")
                .fold(mutableListOf<MutableList<String>>()) { acc, element ->
                    val x = element.text()

                    val start = x.matches("^[A-Z0-9]+ \\[\\+]$".toRegex())

                    if (acc.isEmpty() || start)
                        acc.add(mutableListOf(x))
                    else
                        acc.last().add(x)

                    acc
                }.map { list ->
                    val code = list
                            .first()
                            .replace("[^A-Z0-9]".toRegex(), "")

                    val name = list
                            .last()
                            .substringAfter(" ")
                            .trim()

                    val section = list[1].toIntOrNull() ?: 0

                    val credits = list[2].toIntOrNull() ?: 0

                    val schedule = list
                            .slice(3 until list.size - 1)
                            .chunked(7)
                            .flatMap {
                                val classroom = it.first()

                                it.slice(1 until it.size).mapIndexedNotNull { index, s ->
                                    val dayOfWeek = index + 2

                                    val (startAt, endAt) = when {
                                        s.isBlank() -> listOf(0, 0)
                                        s.contains("-") -> s.split("-").map { x ->
                                            x.toIntOrNull() ?: 0
                                        }
                                        else -> listOf(s.toIntOrNull() ?: 0, s.toIntOrNull() ?: 0)
                                    }

                                    if (startAt != 0 && endAt != 0)
                                        DstScheduleEntry(
                                                dayOfWeek = dayOfWeek,
                                                startAt = startAt,
                                                endAt = endAt,
                                                classroom = classroom
                                        )
                                    else
                                        null
                                }
                            }

                    DstScheduledSubject(
                            code = code,
                            section = section,
                            name = name,
                            credits = credits,
                            schedule = schedule
                    )
                }
    }
}