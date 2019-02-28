package com.gdavidpb.tuindice.utils

import android.net.Uri
import android.util.Base64
import com.gdavidpb.tuindice.data.model.database.QuarterEntity
import com.gdavidpb.tuindice.data.model.database.SubjectEntity
import com.gdavidpb.tuindice.data.source.service.selector.DstAuthResponseSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstEnrollmentDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstPersonalDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstRecordDataSelector
import com.gdavidpb.tuindice.domain.model.*
import com.gdavidpb.tuindice.domain.model.service.*
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.domain.usecase.request.ResetRequest
import com.gdavidpb.tuindice.presentation.model.SummaryCredits
import com.gdavidpb.tuindice.presentation.model.SummaryHeader
import com.gdavidpb.tuindice.presentation.model.SummarySubjects
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

/* Presentation layer */

fun Account.toSummaryHeader(): SummaryHeader {
    return SummaryHeader(
            uid = uid,
            name = fullName.toShortName(),
            photoUrl = photoUrl,
            careerName = careerName,
            grade = grade
    )
}

fun Account.toSummarySubjects(): SummarySubjects {
    return SummarySubjects(
            enrolledSubjects = enrolledSubjects,
            approvedSubjects = approvedSubjects,
            retiredSubjects = retiredSubjects,
            failedSubjects = failedSubjects
    )
}


fun Account.toSummaryCredits(): SummaryCredits {
    return SummaryCredits(
            enrolledCredits = enrolledCredits,
            approvedCredits = approvedCredits,
            retiredCredits = retiredCredits,
            failedCredits = failedCredits
    )
}

fun QuarterEntity.toHeader(): String {
    val start = startDate.toDate().format("MMM")?.capitalize()
    val end = endDate.toDate().format("MMM")?.capitalize()
    val year = startDate.toDate().format("yyyy")

    return "$start - $end $year".replace("\\.".toRegex(), "")
}

/* Data layer */

fun DocumentSnapshot.toAccount(): Account {
    return Account(
            uid = id,
            id = getString(FIELD_USER_ID) ?: "",
            usbId = getString(FIELD_USER_ID) ?: "",
            email = getString(FIELD_USER_EMAIL) ?: "",
            fullName = getString(FIELD_USER_FULL_NAME) ?: "",
            firstNames = getString(FIELD_USER_FIRST_NAMES) ?: "",
            lastNames = getString(FIELD_USER_LAST_NAMES) ?: "",
            careerName = getString(FIELD_USER_CAREER_NAME) ?: "",
            careerCode = getLong(FIELD_USER_CAREER_CODE)?.toInt() ?: 0,
            scholarship = getBoolean(FIELD_USER_SCHOLARSHIP) ?: false,
            grade = getDouble(FIELD_USER_GRADE) ?: 0.0,
            photoUrl = getString(FIELD_USER_PHOTO_URL) ?: "",
            enrolledSubjects = getLong(FIELD_USER_ENROLLED_SUBJECTS)?.toInt() ?: 0,
            enrolledCredits = getLong(FIELD_USER_ENROLLED_CREDITS)?.toInt() ?: 0,
            approvedSubjects = getLong(FIELD_USER_APPROVED_SUBJECT)?.toInt() ?: 0,
            approvedCredits = getLong(FIELD_USER_APPROVED_CREDITS)?.toInt() ?: 0,
            retiredSubjects = getLong(FIELD_USER_RETIRED_SUBJECTS)?.toInt() ?: 0,
            retiredCredits = getLong(FIELD_USER_RETIRED_CREDITS)?.toInt() ?: 0,
            failedSubjects = getLong(FIELD_USER_FAILED_SUBJECTS)?.toInt() ?: 0,
            failedCredits = getLong(FIELD_USER_FAILED_CREDITS)?.toInt() ?: 0
    )
}

/* Service layer */

fun DstQuarter.toQuarterEntity(uid: String): QuarterEntity {
    return QuarterEntity(
            userId = uid,
            startDate = Timestamp(startDate),
            endDate = Timestamp(endDate),
            grade = grade,
            gradeSum = gradeSum,
            status = status
    )
}

fun DstSubject.toSubjectEntity(uid: String, qid: String): SubjectEntity {
    return SubjectEntity(
            userId = uid,
            quarterId = qid,
            code = code,
            name = name,
            credits = credits,
            grade = grade,
            status = status
    )
}

fun DstPersonalDataSelector.toPersonalData(): DstPersonal? {
    return selected
}

fun DstRecordDataSelector.toRecord(): DstRecord? {
    return selected?.run {
        DstRecord(stats = stats, quarters = quarters)
    }
}

fun DstAuthResponseSelector.toAuthResponse(): AuthResponse {
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
            name = fullName.substringAfter(" | ")
    )
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

fun AuthRequest.toDstCredentials(): DstCredentials {
    return DstCredentials(usbId = usbId, password = password)
}

/* Utils */

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