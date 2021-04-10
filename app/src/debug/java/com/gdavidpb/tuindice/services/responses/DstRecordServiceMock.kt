package com.gdavidpb.tuindice.services.responses

import com.gdavidpb.tuindice.data.source.service.selector.DstPersonalDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstRecordDataSelector
import com.gdavidpb.tuindice.domain.model.service.DstPersonal
import com.gdavidpb.tuindice.domain.model.service.DstRecord
import com.gdavidpb.tuindice.domain.model.service.DstRecordStats
import com.gdavidpb.tuindice.utils.STATUS_SUBJECT_NO_EFFECT
import com.gdavidpb.tuindice.utils.STATUS_SUBJECT_RETIRED
import com.gdavidpb.tuindice.utils.createQuarter
import com.gdavidpb.tuindice.utils.createSubject
import com.gdavidpb.tuindice.utils.extensions.parse
import com.gdavidpb.tuindice.utils.mappers.formatSubjectStatusDescription

val defaultPersonalResponse = DstPersonalDataSelector(
        selected = DstPersonal(
                usbId = "1111111",
                id = "0",
                firstNames = "Luis Pedro",
                lastNames = "Pérez Martínez",
                careerCode = 0,
                careerName = "Ingeniería de Computación",
                scholarship = false
        )
)

val septDec2019 = createQuarter(
        startDate = "01-09-2019".parse("dd-MM-yyyy")!!,
        endDate = "01-12-2019".parse("dd-MM-yyyy")!!,
        gradeSum = 3.6428,
        subjects = listOf(
                createSubject(
                        code = "CI3715",
                        name = "INGENIERIA DE SOFTWARE I",
                        credits = 5,
                        grade = 5
                ),
                createSubject(
                        code = "CI3725",
                        name = "TRADUCTORES E INTERPRETADORES",
                        credits = 5,
                        grade = 4
                ),
                createSubject(
                        code = "PS1115",
                        name = "SISTEMAS DE INFORMACION",
                        credits = 4,
                        grade = 3
                )
        )
)

val aprJul2019 = createQuarter(
        startDate = "01-04-2019".parse("dd-MM-yyyy")!!,
        endDate = "01-07-2019".parse("dd-MM-yyyy")!!,
        gradeSum = 3.2143,
        subjects = listOf(
                createSubject(
                        code = "CI2692",
                        name = "LABORATORIO DE ALGORITMOS Y ESTRUCTURAS II",
                        credits = 2,
                        grade = 4
                ),
                createSubject(
                        code = "CI2612",
                        name = "ALGORITMOS Y ESTRUCTURAS II",
                        credits = 3,
                        grade = 4
                ),
                createSubject(
                        code = "CI2527",
                        name = "ESTRUCTURAS DISCRETAS III",
                        credits = 4,
                        grade = 0,
                        status = STATUS_SUBJECT_RETIRED.formatSubjectStatusDescription()
                )
        )
)

val janMar2019 = createQuarter(
        startDate = "01-01-2019".parse("dd-MM-yyyy")!!,
        endDate = "01-03-2019".parse("dd-MM-yyyy")!!,
        gradeSum = 2.7777,
        subjects = listOf(
                createSubject(
                        code = "CI2691",
                        name = "LABORATORIO DE ALGORITMOS Y ESTRUCTURAS I",
                        credits = 2,
                        grade = 4
                ),
                createSubject(
                        code = "CI2611",
                        name = "ALGORITMOS Y ESTRUCTURAS I",
                        credits = 3,
                        grade = 3,
                        status = STATUS_SUBJECT_NO_EFFECT.formatSubjectStatusDescription()
                ),
                createSubject(
                        code = "MA2112",
                        name = "MATEMATICAS V",
                        credits = 4,
                        grade = 2
                )
        )
)

val dstRecordStats = DstRecordStats(grade = 3.6428,
        enrolledSubjects = 23, enrolledCredits = 124, approvedSubjects = 18, approvedCredits = 99,
        retiredSubjects = 3, retiredCredits = 12, failedSubjects = 2, failedCredits = 13)

val dstRecord = DstRecord(
        stats = dstRecordStats,
        quarters = listOf(septDec2019, aprJul2019, janMar2019)
)

val defaultRecordResponse = DstRecordDataSelector(
        selected = dstRecord
)