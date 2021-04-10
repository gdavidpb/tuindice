package com.gdavidpb.tuindice.services.responses

import com.gdavidpb.tuindice.data.source.service.selector.DstEnrollmentDataSelector
import com.gdavidpb.tuindice.domain.model.service.DstPeriod
import com.gdavidpb.tuindice.utils.createScheduledSubject
import com.gdavidpb.tuindice.utils.extensions.parse

val defaultEnrollmentResponse = DstEnrollmentDataSelector(
        schedule = listOf(
                createScheduledSubject(code = "CI4835", name = "REDES DE COMPUTADORAS I", credits = 5),
                createScheduledSubject(code = "CI6450", name = "TOPICOS EN INTELIGENCIA ARTIFICIAL I", credits = 4),
                createScheduledSubject(code = "CI3661", name = "LABORATORIO DE LENGUAJES DE PROGRAMACION I", credits = 2),
                createScheduledSubject(code = "CI3641", name = "LENGUAJES DE PROGRAMACION I", credits = 3),
                createScheduledSubject(code = "CCX154", name = "MUJER Y DESARROLLO INTEGRAL", credits = 3)
        ),
        period = DstPeriod("01-01-2020".parse("dd-MM-yyyy")!!, "01-03-2020".parse("dd-MM-yyyy")!!),
        globalStatus = "",
        enrollmentStatus = ""
)