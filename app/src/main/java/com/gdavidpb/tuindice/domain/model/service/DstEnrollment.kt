package com.gdavidpb.tuindice.domain.model.service

import com.gdavidpb.tuindice.domain.model.QuarterCalendar
import com.gdavidpb.tuindice.domain.model.ScheduleSubject
import java.util.*

data class DstEnrollment(
        val startDate: Date,
        val endDate: Date,
        val schedule: List<ScheduleSubject> = listOf(),
        val calendar: QuarterCalendar,
        val globalStatus: String,
        val enrollmentStatus: String
) : DstData