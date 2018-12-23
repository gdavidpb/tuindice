package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.source.service.selector.DstEnrollmentDataSelector
import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.model.*

open class EnrollmentMapper : Mapper<DstEnrollmentDataSelector, Enrollment> {
    override fun map(value: DstEnrollmentDataSelector): Enrollment {
        val period = value.period.run {
            Period(
                    startDate = startDate,
                    endDate = endDate
            )
        }

        val schedule = value.schedule.run {
            map {
                ScheduleSubject(
                        code = it.code,
                        section = it.section,
                        name = it.name,
                        credits = it.credits,
                        classroom = it.classroom,
                        schedule = it.schedule.map { entry ->
                            ScheduleEntry(
                                    entry.dayOfWeek,
                                    entry.startAt,
                                    entry.endAt
                            )
                        }
                )
            }
        }

        val calendar = value.calendar.run {
            QuarterCalendar(
                    startDate,
                    endDate,
                    correctionDate,
                    giveUpDeadline,
                    degreeRequestDeadline,
                    graduationStartDate,
                    graduationEndDate,
                    documentsRequestDeadline,
                    nextEnrollmentDate,
                    minutesDeliveryDeadline
            )
        }

        return Enrollment(
                period = period,
                calendar = calendar,
                schedule = schedule,
                globalStatus = value.globalStatus,
                enrollmentStatus = value.enrollmentStatus
        )
    }
}