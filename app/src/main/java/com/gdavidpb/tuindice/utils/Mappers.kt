package com.gdavidpb.tuindice.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.TypefaceSpan
import android.util.Base64
import com.gdavidpb.tuindice.R
import com.gdavidpb.tuindice.data.model.database.QuarterEntity
import com.gdavidpb.tuindice.data.model.database.SubjectEntity
import com.gdavidpb.tuindice.data.model.database.SubjectNoGradeEntity
import com.gdavidpb.tuindice.data.source.service.selector.DstAuthResponseSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstEnrollmentDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstPersonalDataSelector
import com.gdavidpb.tuindice.data.source.service.selector.DstRecordDataSelector
import com.gdavidpb.tuindice.domain.model.*
import com.gdavidpb.tuindice.domain.model.service.*
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest
import com.gdavidpb.tuindice.domain.usecase.request.ResetRequest
import com.gdavidpb.tuindice.presentation.model.CustomTypefaceSpan
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

fun Account.toSummaryHeader() = SummaryHeader(
        uid = uid,
        name = fullName.toShortName(),
        photoUrl = photoUrl,
        careerName = careerName,
        grade = grade,
        lastUpdate = lastUpdate
)

fun Account.toSummarySubjects() = SummarySubjects(
        enrolledSubjects = enrolledSubjects,
        approvedSubjects = approvedSubjects,
        retiredSubjects = retiredSubjects,
        failedSubjects = failedSubjects
)

fun Account.toSummaryCredits() = SummaryCredits(
        enrolledCredits = enrolledCredits,
        approvedCredits = approvedCredits,
        retiredCredits = retiredCredits,
        failedCredits = failedCredits
)

fun Subject.toSubjectCode(context: Context) =
        if (status == STATUS_SUBJECT_OK && grade != 0)
            code
        else
            buildSpanned {
                val content = context.getString(R.string.subject_title, code, toSubjectStatusDescription())
                val colorSecondary = context.getCompatColor(R.color.color_secondary_text)

                append(content.substringBefore(' '))
                append(' ')
                append(content.substringAfter(' '),
                        TypefaceSpan("sans-serif-light"),
                        ForegroundColorSpan(colorSecondary))

            }

fun Subject.toSubjectGrade(context: Context) =
        if (grade != 0)
            context.getString(R.string.subject_grade, grade)
        else
            "-"

fun Subject.toSubjectCredits(context: Context) =
        context.getString(R.string.subject_credits, credits)

@SuppressLint("DefaultLocale")
fun Quarter.toQuarterTitle(): String {
    val start = startDate.format("MMM")?.capitalize()
    val end = endDate.format("MMM")?.capitalize()
    val year = startDate.format("yyyy")

    return "$start - $end $year".replace("\\.".toRegex(), "")
}

fun Quarter.toQuarterGradeDiff(color: Int, context: Context) =
        context.getString(R.string.quarter_grade_diff, grade).toSpanned(color)

fun Quarter.toQuarterGradeSum(color: Int, context: Context) =
        context.getString(R.string.quarter_grade_sum, gradeSum).toSpanned(color)

fun Quarter.toQuarterCredits(color: Int, font: Typeface, context: Context) =
        context.getString(R.string.quarter_credits, credits).toSpanned(color, font)

/* Data layer */

fun DocumentSnapshot.toAccount() = Account(
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

fun DocumentSnapshot.toQuarter(subjects: List<Subject>) = Quarter(
        id = id,
        startDate = getTimestamp(FIELD_QUARTER_START_DATE)?.toDate() ?: Date(),
        endDate = getTimestamp(FIELD_QUARTER_END_DATE)?.toDate() ?: Date(),
        grade = getDouble(FIELD_QUARTER_GRADE) ?: 0.0,
        gradeSum = getDouble(FIELD_QUARTER_GRADE_SUM) ?: 0.0,
        credits = subjects.computeCredits(),
        status = getLong(FIELD_QUARTER_STATUS)?.toInt() ?: 0,
        subjects = subjects.toMutableList()
)

fun DocumentSnapshot.toSubject() = Subject(
        id = id,
        qid = getString(FIELD_SUBJECT_QUARTER_ID) ?: "",
        code = getString(FIELD_SUBJECT_CODE) ?: "",
        name = getString(FIELD_SUBJECT_NAME) ?: "",
        credits = getLong(FIELD_SUBJECT_CREDITS)?.toInt() ?: 0,
        grade = getLong(FIELD_SUBJECT_GRADE)?.toInt() ?: 5,
        status = getLong(FIELD_SUBJECT_STATUS)?.toInt() ?: 0
)

/* Service layer */

fun Subject.toSubjectStatusDescription() = when (status) {
    STATUS_SUBJECT_OK -> ""
    STATUS_SUBJECT_RETIRED -> "Retirada"
    STATUS_SUBJECT_GAVE_UP -> "Retirada"
    STATUS_SUBJECT_NO_EFFECT -> "Sin efecto"
    else -> throw IllegalArgumentException("status")
}

fun String.toSubjectStatusValue() = when (this) {
    "" -> STATUS_SUBJECT_OK
    "Retirada" -> STATUS_SUBJECT_RETIRED
    "RETIRADA" -> STATUS_SUBJECT_GAVE_UP
    "Sin Efecto" -> STATUS_SUBJECT_NO_EFFECT
    else -> throw IllegalArgumentException("status")
}

fun DstEnrollment.toQuarterEntity(uid: String) = QuarterEntity(
        userId = uid,
        startDate = Timestamp(startDate),
        endDate = Timestamp(endDate),
        grade = 5.0,
        gradeSum = 0.0,
        status = STATUS_QUARTER_CURRENT
)

fun ScheduleSubject.toSubjectEntity(uid: String, qid: String) = SubjectEntity(
        userId = uid,
        quarterId = qid,
        code = code,
        name = name,
        credits = credits,
        grade = 5,
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

    val defaultQuarterCalendar = QuarterCalendar(
            defaultDate, defaultDate, defaultDate,
            defaultDate, defaultDate, defaultDate,
            defaultDate, defaultDate, defaultDate,
            defaultDate)

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

    val calendar = calendar?.run {
        QuarterCalendar(
                startDate ?: defaultDate,
                endDate ?: defaultDate,
                correctionDate ?: defaultDate,
                giveUpDeadline ?: defaultDate,
                degreeRequestDeadline ?: defaultDate,
                graduationStartDate ?: defaultDate,
                graduationEndDate ?: defaultDate,
                documentsRequestDeadline ?: defaultDate,
                nextEnrollmentDate ?: defaultDate,
                minutesDeliveryDeadline ?: defaultDate
        )
    }

    return DstEnrollment(
            startDate = period?.startDate ?: defaultDate,
            endDate = period?.endDate ?: defaultDate,
            calendar = calendar ?: defaultQuarterCalendar,
            schedule = schedule ?: listOf(),
            globalStatus = globalStatus ?: "",
            enrollmentStatus = enrollmentStatus ?: ""
    )
}

fun AuthRequest.toDstCredentials() = DstCredentials(usbId = usbId, password = password)

/* Utils */

fun FirebaseUser.toAuth() = Auth(
        uid = uid,
        email = email ?: ""
)

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

fun String.toUsbEmail() = "$this@usb.ve"

fun String.toSubjectName(): String {
    var result = replace("^\"|\"$".toRegex(), "")
            .replace("(?<=\\w)\\.(?=\\w)".toRegex(), " ")

    ROMANS.forEach { (key, value) ->
        result = result.replace("(?<=\\b)$key(?=\\b|$)".toRegex(), value)
    }

    return result
}

/* Internal utils */

private fun String.toSpanned(color: Int, font: Typeface? = null): Spanned {
    val (iconString, valueString, extraString) = split(' ')
            .toMutableList()
            .apply { if (size == 2) add("") }

    val typefaceSpan = font?.let(::CustomTypefaceSpan) ?: TypefaceSpan("sans-serif-medium")

    return buildSpanned {
        append(iconString,
                typefaceSpan,
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