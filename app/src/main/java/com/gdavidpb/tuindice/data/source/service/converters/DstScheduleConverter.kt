package com.gdavidpb.tuindice.data.source.service.converters

import com.gdavidpb.tuindice.domain.model.service.DstScheduleEntry
import com.gdavidpb.tuindice.domain.model.service.DstScheduledSubject
import com.gdavidpb.tuindice.utils.mappers.parseSubjectName
import org.jsoup.nodes.Element
import pl.droidsonroids.jspoon.ElementConverter
import pl.droidsonroids.jspoon.annotation.Selector

class DstScheduleConverter : ElementConverter<List<DstScheduledSubject>> {
    override fun convert(node: Element, selector: Selector): List<DstScheduledSubject> {
        val subjectStartRegex = "^[A-Z0-9]+\\s*\\[\\+]$".toRegex()
        val subjectEndRegex = "^\\[[A-Z0-9]+]\\s*[^$]+$".toRegex()

        return node
                .select("td")
                .fold(mutableListOf<MutableList<String>>()) { acc, element ->
                    val text = element.text()

                    val isCarryingOut = acc.isNotEmpty()

                    val isSubjectStart = text.matches(subjectStartRegex)
                    val isSubjectEnd = text.matches(subjectEndRegex)

                    when {
                        isSubjectStart -> acc.add(mutableListOf(text))
                        isSubjectEnd || isCarryingOut -> acc.last().add(text)
                    }

                    acc
                }.map { list ->
                    val code = list
                            .first()
                            .replace("[^A-Z0-9]".toRegex(), "")

                    val name = list
                            .last()
                            .substringAfter(' ')
                            .trim()

                    val section = list[1].toIntOrNull() ?: 0

                    val credits = list[2].toIntOrNull() ?: 0

                    val hasStatus = (list.size > 4)

                    val status = if (hasStatus) list[list.size - 2] else ""

                    val isScheduleOk = hasStatus && status.isEmpty()

                    val schedule = if (isScheduleOk) list.parseSchedule() else listOf()

                    DstScheduledSubject(
                            code = code,
                            section = section,
                            name = name.parseSubjectName(),
                            credits = credits,
                            status = status,
                            schedule = schedule
                    )
                }
    }

    private fun List<String>.parseSchedule(): List<DstScheduleEntry> {
        return slice(3 until size - 1)
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
    }
}