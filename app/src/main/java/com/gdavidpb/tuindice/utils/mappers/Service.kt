package com.gdavidpb.tuindice.utils.mappers

import com.gdavidpb.tuindice.data.model.database.QuarterEntity
import com.gdavidpb.tuindice.data.model.database.SubjectEntity
import com.gdavidpb.tuindice.data.model.database.SubjectNoGradeEntity
import com.gdavidpb.tuindice.data.source.service.selector.DstAuthResponseSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstEnrollmentDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstPersonalDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstRecordDataSelector
import com.gdavidpb.tuindice.domain.model.*
import com.gdavidpb.tuindice.domain.model.service.*
import com.gdavidpb.tuindice.utils.MAX_GRADE
import com.gdavidpb.tuindice.utils.STATUS_QUARTER_CURRENT
import com.google.firebase.Timestamp
import java.util.*

/* Service layer */

fun DstEnrollment.toQuarterEntity(uid: String) = QuarterEntity(
        userId = uid,
        startDate = Timestamp(startDate),
        endDate = Timestamp(endDate),
        grade = MAX_GRADE.toDouble(),
        gradeSum = 0.0,
        status = STATUS_QUARTER_CURRENT
)

fun ScheduleSubject.toSubjectEntity(uid: String, qid: String) = SubjectEntity(
        userId = uid,
        quarterId = qid,
        code = code,
        name = name,
        credits = credits,
        grade = MAX_GRADE,
        status = status.toSubjectStatusValue()
)

fun SubjectEntity.toNoGrade() = SubjectNoGradeEntity(
        userId = userId,
        quarterId = quarterId,
        code = code,
        name = name,
        credits = credits,
        status = status
)

fun DstQuarter.toQuarterEntity(uid: String) = QuarterEntity(
        userId = uid,
        startDate = Timestamp(startDate),
        endDate = Timestamp(endDate),
        grade = grade,
        gradeSum = gradeSum,
        status = status
)

fun DstSubject.toSubjectEntity(uid: String, qid: String) = SubjectEntity(
        userId = uid,
        quarterId = qid,
        code = code,
        name = name,
        credits = credits,
        grade = grade,
        status = status.toSubjectStatusValue()
)

fun DstPersonalDataSelector.toPersonalData() = selected

fun DstRecordDataSelector.toRecord() = selected?.run {
    DstRecord(stats = stats, quarters = quarters)
}

fun DstAuthResponseSelector.toAuthResponse(): AuthResponse {
    val message = arrayOf(
            invalidCredentialsMessage,
            noEnrolledMessage,
            expiredSessionMessage
    ).firstOrNull {
        it.isNotEmpty()
    } ?: ""

    val code = when {
        invalidCredentialsMessage.isNotEmpty() -> AuthResponseCode.INVALID_CREDENTIALS
        noEnrolledMessage.isNotEmpty() -> AuthResponseCode.NO_ENROLLED
        expiredSessionMessage.isNotEmpty() -> AuthResponseCode.SESSION_EXPIRED
        else -> AuthResponseCode.SUCCESS
    }

    return AuthResponse(
            isSuccessful = code == AuthResponseCode.SUCCESS || code == AuthResponseCode.NO_ENROLLED,
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
                    name = it.name.toSubjectName(),
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