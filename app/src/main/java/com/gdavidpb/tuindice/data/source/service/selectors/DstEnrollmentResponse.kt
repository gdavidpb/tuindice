package com.gdavidpb.tuindice.data.source.service.selectors

import com.gdavidpb.tuindice.domain.model.service.DstScheduledSubject
import com.gdavidpb.tuindice.data.source.service.converters.DstPeriodConverter
import com.gdavidpb.tuindice.data.source.service.converters.DstScheduleConverter
import com.gdavidpb.tuindice.domain.model.service.DstPeriod
import pl.droidsonroids.jspoon.annotation.Selector

data class DstEnrollmentResponse(
        @Selector(value = "#horario table", converter = DstScheduleConverter::class)
        var schedule: List<DstScheduledSubject>? = null,
        @Selector(value = "#horario strong", converter = DstPeriodConverter::class)
        var period: DstPeriod? = null,
        @Selector(value = "td:first-child td")
        var globalStatus: String? = null,
        @Selector(value = "td:last-child td")
        var enrollmentStatus: String? = null
)