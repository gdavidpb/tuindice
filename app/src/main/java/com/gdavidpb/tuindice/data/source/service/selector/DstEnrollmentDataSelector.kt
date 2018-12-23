package com.gdavidpb.tuindice.data.source.service.selector

import com.gdavidpb.tuindice.data.model.service.DstCalendar
import com.gdavidpb.tuindice.data.model.service.DstPeriod
import com.gdavidpb.tuindice.data.model.service.DstScheduledSubject
import com.gdavidpb.tuindice.data.source.service.converter.DstCalendarConverter
import com.gdavidpb.tuindice.data.source.service.converter.DstPeriodConverter
import com.gdavidpb.tuindice.data.source.service.converter.DstScheduleConverter
import pl.droidsonroids.jspoon.annotation.Selector

open class DstEnrollmentDataSelector {
    @Selector("#calendario table", converter = DstCalendarConverter::class)
    lateinit var calendar: DstCalendar

    @Selector("#horario table", converter = DstScheduleConverter::class)
    lateinit var schedule: List<DstScheduledSubject>

    @Selector("#horario strong", converter = DstPeriodConverter::class)
    lateinit var period: DstPeriod

    @Selector("td:first-child td")
    lateinit var globalStatus: String

    @Selector("td:last-child td")
    lateinit var enrollmentStatus: String
}