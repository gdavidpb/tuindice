package com.gdavidpb.tuindice.utils

import android.content.Context
import android.net.Uri
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.util.Base64
import androidx.core.content.ContextCompat
import com.gdavidpb.tuindice.R
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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot
import org.jetbrains.anko.append
import org.jetbrains.anko.buildSpanned
import org.jetbrains.anko.foregroundColor
import java.util.*

/* Presentation layer */

fun Account.toSummaryHeader(): SummaryHeader {
    return SummaryHeader(
            uid = uid,
            name = fullName.toShortName(),
            photoUrl = photoUrl,
            careerName = careerName,
            grade = grade,
            lastUpdate = lastUpdate
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

fun Subject.toSubjectCode(context: Context): CharSequence {
    return if (status == STATUS_SUBJECT_OK)
        code
    else
        buildSpanned {
            val content = context.getString(R.string.subjectTitle, code, status.toSubjectStatusDescription())
            val colorSecondary = ContextCompat.getColor(context, R.color.colorSecondaryText)

            append(content.substringBefore(' '))
            append(' ')
            append(content.substringAfter(' '),
                    TypefaceSpan("sans-serif-light"),
                    ForegroundColorSpan(colorSecondary))

        }
}

fun Subject.toSubjectName(): String {
    var result = name
            .replace("^\"|\"$".toRegex(), "")
            .replace("(?<=\\w)\\.(?=\\w)".toRegex(), " ")

    ROMANS.forEach { (key, value) ->
        result = result.replace("(?<=\\b)$key(?=\\b|$)".toRegex(), value)
    }

    return result
}

fun Subject.toSubjectGrade(context: Context): String {
    return if (grade != 0)
        return context.getString(R.string.subjectGrade, grade)
    else
        "-"
}

fun Subject.toSubjectCredits(context: Context): String {
    return context.getString(R.string.subjectCredits, credits)
}

fun Quarter.toQuarterTitle(): String {
    val start = startDate.format("MMM")?.capitalize()
    val end = endDate.format("MMM")?.capitalize()
    val year = startDate.format("yyyy")

    return "$start - $end $year".replace("\\.".toRegex(), "")
}

fun Quarter.toQuarterGradeDiff(color: Int, context: Context): CharSequence {
    return context.getString(R.string.quarterGradeDiff, grade).toSpanned(color)
}

fun Quarter.toQuarterGradeSum(color: Int, context: Context): CharSequence {
    return context.getString(R.string.quarterGradeSum, gradeSum).toSpanned(color)
}

fun Quarter.toQuarterCredits(color: Int, context: Context): CharSequence {
    return context.getString(R.string.quarterCredits, credits).toSpanned(color)
}

fun String.toSpanned(color: Int): Spanned {
    val (iconString, valueString, extraString) = split(' ')
            .toMutableList()
            .apply { if (size == 2) add("") }

    return buildSpanned {
        append(iconString,
                TypefaceSpan("sans-serif-medium"),
                AbsoluteSizeSpan(18, true),
                foregroundColor(color))
        append(' ')
        append(valueString)

        if (extraString.isNotBlank()) {
            append(' ')
            append(extraString)
        }
    }
}

/* Data layer */

fun DocumentSnapshot.toAccount(lastUpdate: Date): Account {
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
            failedCredits = getLong(FIELD_USER_FAILED_CREDITS)?.toInt() ?: 0,
            lastUpdate = lastUpdate
    )
}

fun DocumentSnapshot.toQuarter(subjects: List<Subject>): Quarter {
    return Quarter(
            id = id,
            startDate = getTimestamp(FIELD_QUARTER_START_DATE)?.toDate() ?: Date(),
            endDate = getTimestamp(FIELD_QUARTER_END_DATE)?.toDate() ?: Date(),
            grade = getDouble(FIELD_QUARTER_GRADE) ?: 0.0,
            gradeSum = getDouble(FIELD_QUARTER_GRADE_SUM) ?: 0.0,
            credits = subjects.sumBy { it.credits },
            status = getLong(FIELD_QUARTER_STATUS)?.toInt() ?: 0,
            subjects = subjects
    )
}

fun DocumentSnapshot.toSubject(): Subject {
    return Subject(
            id = id,
            qid = getString(FIELD_SUBJECT_QUARTER_ID) ?: "",
            code = getString(FIELD_SUBJECT_CODE) ?: "",
            name = getString(FIELD_SUBJECT_NAME) ?: "",
            credits = getLong(FIELD_SUBJECT_CREDITS)?.toInt() ?: 0,
            grade = getLong(FIELD_SUBJECT_GRADE)?.toInt() ?: 0,
            status = getLong(FIELD_SUBJECT_STATUS)?.toInt() ?: 0
    )
}

/* Service layer */

fun Int.toSubjectStatusDescription(): String {
    return when (this) {
        STATUS_SUBJECT_OK -> ""
        STATUS_SUBJECT_RETIRED -> "Retirada"
        STATUS_QUARTER_NO_EFFECT -> "Sin efecto"
        else -> throw IllegalArgumentException("status")
    }
}

fun String.toSubjectStatusValue(): Int {
    return when (this) {
        "" -> STATUS_SUBJECT_OK
        "Retirada" -> STATUS_SUBJECT_RETIRED
        "Sin Efecto" -> STATUS_QUARTER_NO_EFFECT
        else -> throw IllegalArgumentException("status")
    }
}

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
            status = status.toSubjectStatusValue()
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

fun FirebaseUser.toAuth(): Auth {
    return Auth(
            uid = uid,
            email = email ?: ""
    )
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