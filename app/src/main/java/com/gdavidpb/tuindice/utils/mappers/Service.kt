package com.gdavidpb.tuindice.utils.mappers

import com.gdavidpb.tuindice.data.source.service.selector.DstAuthResponseSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstEnrollmentDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstPersonalDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstRecordDataSelector
import com.gdavidpb.tuindice.domain.model.AuthResponseCode
import com.gdavidpb.tuindice.domain.model.ScheduleEntry
import com.gdavidpb.tuindice.domain.model.ScheduleSubject
import com.gdavidpb.tuindice.domain.model.SignInResponse
import com.gdavidpb.tuindice.domain.model.service.DstEnrollment
import com.gdavidpb.tuindice.domain.model.service.DstRecord
import java.util.*

fun DstPersonalDataSelector.toPersonalData() = selected

fun DstRecordDataSelector.toRecord() = selected?.run {
    DstRecord(stats = stats, quarters = quarters)
}

fun DstAuthResponseSelector.toAuthResponse(): SignInResponse {
    val message = arrayOf(
            invalidCredentialsMessage,
            notEnrolledMessage,
            expiredSessionMessage
    ).firstOrNull {
        it.isNotEmpty()
    } ?: ""

    val code = when {
        invalidCredentialsMessage.isNotEmpty() -> AuthResponseCode.INVALID_CREDENTIALS
        notEnrolledMessage.isNotEmpty() -> AuthResponseCode.NOT_ENROLLED
        expiredSessionMessage.isNotEmpty() -> AuthResponseCode.SESSION_EXPIRED
        else -> AuthResponseCode.SUCCESS
    }

    return SignInResponse(
            isSuccessful = code == AuthResponseCode.SUCCESS || code == AuthResponseCode.NOT_ENROLLED,
            code = code,
            message = message,
            name = fullName.substringAfter(" | ")
    )
}

fun DstEnrollmentDataSelector.toEnrollment(): DstEnrollment {
    val defaultDate = Date(0)

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
            startDate = period?.startDate ?: defaultDate,
            endDate = period?.endDate ?: defaultDate,
            schedule = schedule ?: listOf(),
            globalStatus = globalStatus ?: "",
            enrollmentStatus = enrollmentStatus ?: ""
    )
}