package com.gdavidpb.tuindice.domain.model

data class Enrollment(
        val period: Period,
        val schedule: List<ScheduleSubject>,
        val calendar: QuarterCalendar,
        val globalStatus: String,
        val enrollmentStatus: String
)