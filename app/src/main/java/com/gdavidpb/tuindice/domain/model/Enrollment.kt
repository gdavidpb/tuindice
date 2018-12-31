package com.gdavidpb.tuindice.domain.model

data class Enrollment(
        val period: Period = Period(),
        val schedule: List<ScheduleSubject> = listOf(),
        val calendar: QuarterCalendar,
        val globalStatus: String = "",
        val enrollmentStatus: String = ""
)