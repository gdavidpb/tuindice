package com.gdavidpb.tuindice.utils.mappers

import com.gdavidpb.tuindice.BuildConfig
import com.gdavidpb.tuindice.domain.model.*
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.computeCredits
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import java.util.*

fun DocumentSnapshot.toAccount() = Account(
        uid = id,
        id = getString(UserCollection.ID) ?: "",
        usbId = getString(UserCollection.USB_ID) ?: "",
        email = getString(UserCollection.EMAIL) ?: "",
        fullName = getString(UserCollection.FULL_NAME) ?: "",
        firstNames = getString(UserCollection.FIRST_NAMES) ?: "",
        lastNames = getString(UserCollection.LAST_NAMES) ?: "",
        careerName = getString(UserCollection.CAREER_NAME) ?: "",
        careerCode = getLong(UserCollection.CAREER_CODE)?.toInt() ?: 0,
        scholarship = getBoolean(UserCollection.SCHOLARSHIP) ?: false,
        grade = getDouble(UserCollection.GRADE) ?: 0.0,
        enrolledSubjects = getLong(UserCollection.ENROLLED_SUBJECTS)?.toInt() ?: 0,
        enrolledCredits = getLong(UserCollection.ENROLLED_CREDITS)?.toInt() ?: 0,
        approvedSubjects = getLong(UserCollection.APPROVED_SUBJECT)?.toInt() ?: 0,
        approvedCredits = getLong(UserCollection.APPROVED_CREDITS)?.toInt() ?: 0,
        retiredSubjects = getLong(UserCollection.RETIRED_SUBJECTS)?.toInt() ?: 0,
        retiredCredits = getLong(UserCollection.RETIRED_CREDITS)?.toInt() ?: 0,
        failedSubjects = getLong(UserCollection.FAILED_SUBJECTS)?.toInt() ?: 0,
        failedCredits = getLong(UserCollection.FAILED_CREDITS)?.toInt() ?: 0,
        approvedRelation = getDouble(UserCollection.APPROVED_RELATION) ?: 0.0,
        lastUpdate = getDate(UserCollection.LAST_UPDATE) ?: Date(0),
        appVersionCode = getLong(UserCollection.APP_VERSION_CODE)?.toInt() ?: 0
)

// TODO set credits default value to 0
fun DocumentSnapshot.toQuarter(subjects: List<Subject>) = Quarter(
        id = id,
        startDate = getDate(QuarterCollection.START_DATE) ?: Date(),
        endDate = getDate(QuarterCollection.END_DATE) ?: Date(),
        grade = getDouble(QuarterCollection.GRADE) ?: 0.0,
        gradeSum = getDouble(QuarterCollection.GRADE_SUM) ?: 0.0,
        credits = getLong(QuarterCollection.CREDITS)?.toInt() ?: subjects.computeCredits(),
        status = getLong(QuarterCollection.STATUS)?.toInt() ?: 0,
        subjects = subjects.toMutableList()
)

fun DocumentSnapshot.toSubject(): Subject {
    val grade = getLong(SubjectCollection.GRADE)?.toInt() ?: MAX_SUBJECT_GRADE
    val status = getLong(SubjectCollection.STATUS)?.toInt() ?: STATUS_SUBJECT_OK

    return Subject(
            id = id,
            qid = getString(SubjectCollection.QUARTER_ID) ?: "",
            code = getString(SubjectCollection.CODE) ?: "",
            name = getString(SubjectCollection.NAME) ?: "",
            credits = getLong(SubjectCollection.CREDITS)?.toInt() ?: 0,
            grade = grade,
            status = if (grade != 0) status else STATUS_SUBJECT_RETIRED
    )
}

fun DocumentSnapshot.toEvaluation() = Evaluation(
        id = id,
        sid = getString(EvaluationCollection.SUBJECT_ID) ?: "",
        subjectCode = getString(EvaluationCollection.SUBJECT_CODE) ?: "",
        type = EvaluationType.values()[(getLong(EvaluationCollection.TYPE)?.toInt()
                ?: EvaluationType.OTHER.ordinal)],
        grade = getDouble(EvaluationCollection.GRADE) ?: 0.0,
        maxGrade = getDouble(EvaluationCollection.MAX_GRADE) ?: 0.0,
        date = getDate(EvaluationCollection.DATE) ?: Date(0),
        notes = getString(EvaluationCollection.NOTES) ?: "",
        isDone = getBoolean(EvaluationCollection.DONE) ?: false
)

fun Account.toAccountEntity() = mapOf(
        UserCollection.ID to id,
        UserCollection.USB_ID to usbId,
        UserCollection.EMAIL to email,
        UserCollection.FULL_NAME to fullName,
        UserCollection.FIRST_NAMES to firstNames,
        UserCollection.LAST_NAMES to lastNames,
        UserCollection.CAREER_NAME to careerName,
        UserCollection.CAREER_CODE to careerCode,
        UserCollection.SCHOLARSHIP to scholarship,
        UserCollection.GRADE to grade,
        UserCollection.ENROLLED_SUBJECTS to enrolledSubjects,
        UserCollection.ENROLLED_CREDITS to enrolledCredits,
        UserCollection.APPROVED_SUBJECT to approvedSubjects,
        UserCollection.APPROVED_CREDITS to approvedCredits,
        UserCollection.RETIRED_SUBJECTS to retiredSubjects,
        UserCollection.RETIRED_CREDITS to retiredCredits,
        UserCollection.FAILED_SUBJECTS to failedSubjects,
        UserCollection.FAILED_CREDITS to failedCredits,
        UserCollection.APPROVED_RELATION to approvedCredits.toDouble() / enrolledCredits.toDouble(),
        UserCollection.LAST_UPDATE to FieldValue.serverTimestamp(),
        UserCollection.APP_VERSION_CODE to BuildConfig.VERSION_CODE
)

fun Quarter.toQuarterEntity(uid: String) = mapOf(
        QuarterCollection.USER_ID to uid,
        QuarterCollection.START_DATE to Timestamp(startDate),
        QuarterCollection.END_DATE to Timestamp(endDate),
        QuarterCollection.GRADE to grade,
        QuarterCollection.GRADE_SUM to gradeSum,
        QuarterCollection.CREDITS to credits,
        QuarterCollection.STATUS to status
)

fun Subject.toSubjectEntity(uid: String) = mapOf(
        SubjectCollection.USER_ID to uid,
        SubjectCollection.QUARTER_ID to qid,
        SubjectCollection.CODE to code,
        SubjectCollection.NAME to name,
        SubjectCollection.CREDITS to credits,
        SubjectCollection.GRADE to grade,
        SubjectCollection.STATUS to status
)

fun Evaluation.toEvaluationEntity(uid: String) = mutableMapOf(
        EvaluationCollection.USER_ID to uid,
        EvaluationCollection.SUBJECT_ID to sid,
        EvaluationCollection.SUBJECT_CODE to subjectCode,
        EvaluationCollection.TYPE to type.ordinal,
        EvaluationCollection.GRADE to grade,
        EvaluationCollection.MAX_GRADE to maxGrade,
        EvaluationCollection.DONE to isDone,
        EvaluationCollection.LAST_MODIFIED to FieldValue.serverTimestamp()
).apply {
    if (date.time != 0L) put(EvaluationCollection.DATE, Timestamp(date))
    if (notes.isNotBlank()) put(EvaluationCollection.NOTES, notes.trim().take(MAX_EVALUATION_NOTES))
}