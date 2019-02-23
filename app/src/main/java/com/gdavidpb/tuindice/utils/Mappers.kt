package com.gdavidpb.tuindice.utils

import android.net.Uri
import android.util.Base64
import com.gdavidpb.tuindice.data.model.service.DstCredentials
import com.gdavidpb.tuindice.data.model.service.DstPeriod
import com.gdavidpb.tuindice.data.model.service.DstQuarter
import com.gdavidpb.tuindice.data.model.service.DstSubject
import com.gdavidpb.tuindice.data.source.service.selector.DstAuthResponseSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstEnrollmentDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstPersonalDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstRecordDataSelector
import com.gdavidpb.tuindice.domain.model.*
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.domain.usecase.request.ResetRequest
import java.util.*

fun DstPersonalDataSelector.toAccount(): Account? {
    return selected?.run {
        Account(
                id = id,
                usbId = usbId,
                firstNames = firstNames,
                lastNames = lastNames,
                scholarship = scholarship,
                careerName = career.name,
                careerCode = career.code
        )
    }
}

fun DstAuthResponseSelector.toAuthResponse(request: AuthRequest): AuthResponse {
    val message = arrayOf(
            invalidCredentialsMessage,
            noEnrolledMessage,
            expiredSessionMessage
    ).firstOrNull {
        !it.isEmpty()
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
            name = fullName.substringAfter(" | "),
            request = request
    )
}

fun AuthRequest.toDstCredentials(): DstCredentials {
    return DstCredentials(usbId = usbId, password = password)
}

fun DstEnrollmentDataSelector.toEnrollment(): Enrollment {
    val default = Date(0)

    val period = period.run {
        Period(
                startDate = startDate,
                endDate = endDate
        )
    }

    val schedule = schedule.run {
        map {
            ScheduleSubject(
                    code = it.code,
                    section = it.section,
                    name = it.name,
                    credits = it.credits,
                    classroom = it.classroom,
                    schedule = it.schedule.map { entry ->
                        ScheduleEntry(
                                entry.dayOfWeek,
                                entry.startAt,
                                entry.endAt
                        )
                    }
            )
        }
    }

    val calendar = calendar.run {
        QuarterCalendar(
                startDate ?: default,
                endDate ?: default,
                correctionDate ?: default,
                giveUpDeadline ?: default,
                degreeRequestDeadline ?: default,
                graduationStartDate ?: default,
                graduationEndDate ?: default,
                documentsRequestDeadline ?: default,
                nextEnrollmentDate ?: default,
                minutesDeliveryDeadline ?: default
        )
    }

    return Enrollment(
            period = period,
            calendar = calendar,
            schedule = schedule,
            globalStatus = globalStatus,
            enrollmentStatus = enrollmentStatus
    )
}

fun DstPeriod.toPeriod(): Period {
    return Period(
            startDate = startDate,
            endDate = endDate
    )
}

fun DstSubject.toSubject(): Subject {
    return Subject(
            code = code,
            name = name,
            credits = credits,
            grade = grade,
            status = status
    )
}

fun DstQuarter.toQuarter(): Quarter {
    return Quarter(
            period = period.toPeriod(),
            subjects = subjects.map { it.toSubject() },
            grade = grade,
            gradeSum = gradeSum
    )
}

fun DstRecordDataSelector.toRecord(): Record {
    return selected.run {
        Record(
                quarters = quarters.map { it.toQuarter() },
                enrolledCredits = enrolledCredits,
                enrolledSubjects = enrolledSubjects,
                approvedCredits = approvedCredits,
                approvedSubject = approvedSubject,
                retiredCredits = retiredCredits,
                retiredSubjects = retiredSubjects,
                failedCredits = failedCredits,
                failedSubjects = failedSubjects
        )
    }
}

fun String.toResetRequest(): ResetRequest {
    fun getCode(uri: Uri) = uri.getQueryParameter("oobCode")!!
    fun getContinueUrl(uri: Uri) = uri.getQueryParameter("continueUrl")!!

    val mainUri = Uri.parse(this)

    val code = getCode(mainUri)
    val continueUrl = getContinueUrl(mainUri)

    val continueUri = Uri.parse(continueUrl)

    val resetPassword = continueUri.getQueryParameter("")!!

    val (email, password) = resetPassword.toResetParam()

    return ResetRequest(code, email, password)
}

fun String.toResetParam(): Pair<String, String> {
    val data = String(Base64.decode(this, Base64.DEFAULT)).split("\n")

    return data.first() to data.last()
}

fun Pair<String, String>.fromResetParam(): String {
    val data = "$first\n$second".toByteArray()

    return Base64.encodeToString(data, Base64.DEFAULT)
}

fun String.toUsbEmail(): String {
    return "$this@usb.ve"
}