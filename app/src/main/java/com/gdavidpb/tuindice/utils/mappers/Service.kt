package com.gdavidpb.tuindice.utils.mappers

import com.gdavidpb.tuindice.data.source.service.responses.*
import com.gdavidpb.tuindice.domain.model.AuthResponseCode
import com.gdavidpb.tuindice.domain.model.ScheduleEntry
import com.gdavidpb.tuindice.domain.model.ScheduleSubject
import com.gdavidpb.tuindice.domain.model.service.DstAuth
import com.gdavidpb.tuindice.domain.model.service.DstEnrollment
import com.gdavidpb.tuindice.domain.model.service.DstRecord

fun DstPersonalResponse.toPersonal() = selected ?: error("toPersonalData")

fun DstRecordResponse.toRecord() = selected?.run {
    DstRecord(stats = stats, quarters = quarters)
} ?: error("toRecord")

fun DstAuthResponse.toAuth(): DstAuth {
    val (code, message) = when {
        invalidCredentialsMessage.isNotEmpty() -> AuthResponseCode.INVALID_CREDENTIALS to invalidCredentialsMessage
        notEnrolledMessage.isNotEmpty() -> AuthResponseCode.NOT_ENROLLED to notEnrolledMessage
        expiredSessionMessage.isNotEmpty() -> AuthResponseCode.SESSION_EXPIRED to expiredSessionMessage
        else -> AuthResponseCode.SUCCESS to ""
    }

    return DstAuth(
            isSuccessful = (code == AuthResponseCode.SUCCESS),
            code = code,
            message = message
    )
}

fun DstEnrollmentResponse.toEnrollment(): DstEnrollment {
    val schedule = schedule?.run {
        map {
            ScheduleSubject(
                    code = it.code,
                    section = it.section,
                    name = it.name,
                    credits = it.credits,
                    status = it.status,
                    schedule = it.schedule.map { entry ->
                        ScheduleEntry(
                                entry.dayOfWeek,
                                entry.startAt,
                                entry.endAt,
                                entry.classroom
                        )
                    }
            )
        }
    }

    return DstEnrollment(
            startDate = period?.startDate ?: error("toEnrollment"),
            endDate = period?.endDate ?: error("toEnrollment"),
            schedule = schedule ?: listOf(),
            globalStatus = globalStatus ?: "",
            enrollmentStatus = enrollmentStatus ?: ""
    )
}