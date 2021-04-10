package com.gdavidpb.tuindice.data.source.service.selector

import com.gdavidpb.tuindice.domain.model.service.DstScheduledSubject
import com.gdavidpb.tuindice.data.source.service.converter.DstPeriodConverter
import com.gdavidpb.tuindice.data.source.service.converter.DstScheduleConverter
import com.gdavidpb.tuindice.domain.model.service.DstPeriod
import pl.droidsonroids.jspoon.annotation.Selector

data class DstEnrollmentDataSelector(
        @Selector(value = "#horario table", converter = DstScheduleConverter::class)
        var schedule: List<DstScheduledSubject>? = null,
        @Selector(value = "#horario strong", converter = DstPeriodConverter::class)
        var period: DstPeriod? = null,
        @Selector(value = "td:first-child td")
        var globalStatus: String? = null,
        @Selector(value = "td:last-child td")
        var enrollmentStatus: String? = null
)