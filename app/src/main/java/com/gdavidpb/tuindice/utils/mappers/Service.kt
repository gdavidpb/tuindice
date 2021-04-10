package com.gdavidpb.tuindice.utils.mappers

import com.gdavidpb.tuindice.data.source.service.selector.DstAuthResponseSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstEnrollmentDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstPersonalDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstRecordDataSelector
import com.gdavidpb.tuindice.domain.model.AuthResponseCode
import com.gdavidpb.tuindice.domain.model.ScheduleEntry
import com.gdavidpb.tuindice.domain.model.ScheduleSubject
import com.gdavidpb.tuindice.domain.model.SignInResponse
import com.gdavidpb.tuindice.domain.model.exception.ParseException
import com.gdavidpb.tuindice.domain.model.service.DstEnrollment
import com.gdavidpb.tuindice.domain.model.service.DstRecord

fun DstPersonalDataSelector.toPersonalData() = selected ?: throw ParseException("toPersonalData")

fun DstRecordDataSelector.toRecord() = selected?.run {
    DstRecord(stats = stats, quarters = quarters)
} ?: throw ParseException("toRecord")

fun DstAuthResponseSelector.toAuthResponse(): SignInResponse {
    val (code, message) = when {
        invalidCredentialsMessage.isNotEmpty() -> AuthResponseCode.INVALID_CREDENTIALS to invalidCredentialsMessage
        notEnrolledMessage.isNotEmpty() -> AuthResponseCode.NOT_ENROLLED to notEnrolledMessage
        expiredSessionMessage.isNotEmpty() -> AuthResponseCode.SESSION_EXPIRED to expiredSessionMessage
        else -> AuthResponseCode.SUCCESS to ""
    }

    return SignInResponse(
            isSuccessful = (code == AuthResponseCode.SUCCESS),
            code = code,
            message = message,
            fullName = fullName.substringAfter(" | ")
    )
}

fun DstEnrollmentDataSelector.toEnrollment(): DstEnrollment {
    val schedule = schedule?.run {
        map {
            ScheduleSubject(
                    code = it.code,
                    section = it.section,
                    name = it.name.formatSubjectName(),
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
            startDate = period?.startDate ?: throw ParseException("toEnrollment"),
            endDate = period?.endDate ?: throw ParseException("toEnrollment"),
            schedule = schedule ?: listOf(),
            globalStatus = globalStatus ?: "",
            enrollmentStatus = enrollmentStatus ?: ""
    )
}