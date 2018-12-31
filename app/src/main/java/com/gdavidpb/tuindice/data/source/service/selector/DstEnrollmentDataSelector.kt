package com.gdavidpb.tuindice.data.source.service.selector

import com.gdavidpb.tuindice.data.model.service.DstQuarterCalendar
import com.gdavidpb.tuindice.data.model.service.DstPeriod
import com.gdavidpb.tuindice.data.model.service.DstScheduledSubject
import com.gdavidpb.tuindice.data.source.service.converter.DstCalendarConverter
import com.gdavidpb.tuindice.data.source.service.converter.DstPeriodConverter
import com.gdavidpb.tuindice.data.source.service.converter.DstScheduleConverter
import pl.droidsonroids.jspoon.annotation.Selector

data class DstEnrollmentDataSelector(
        @Selector("#calendario table", converter = DstCalendarConverter::class)
        var calendar: DstQuarterCalendar,
        @Selector("#horario table", converter = DstScheduleConverter::class)
        var schedule: List<DstScheduledSubject>,
        @Selector("#horario strong", converter = DstPeriodConverter::class)
        var period: DstPeriod,
        @Selector("td:first-child td")
        var globalStatus: String,
        @Selector("td:last-child td")
        var enrollmentStatus: String
)