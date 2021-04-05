package com.gdavidpb.tuindice.utils.mappers

import com.gdavidpb.tuindice.data.model.database.EvaluationEntity
import com.gdavidpb.tuindice.data.model.database.QuarterEntity
import com.gdavidpb.tuindice.data.model.database.SubjectEntity
import com.gdavidpb.tuindice.domain.model.*
import com.gdavidpb.tuindice.domain.model.service.DstEnrollment
import com.gdavidpb.tuindice.domain.model.service.DstQuarter
import com.gdavidpb.tuindice.domain.model.service.DstSubject
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.base64
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

/* Identifiers generation */

private val digestConcat = DigestConcat(algorithm = "SHA-256")

fun QuarterEntity.generateId() = digestConcat
        .concat(data = userId)
        .concat(data = quarterTitle())
        .build()
        .base64()
        .replace("[/+=\n]+".toRegex(), "")
        .substring(userId.indices)

fun SubjectEntity.generateId() = digestConcat
        .concat(data = quarterId)
        .concat(data = code)
        .build()
        .base64()
        .replace("[/+=\n]+".toRegex(), "")
        .substring(userId.indices)

/* Read from database */

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
        lastUpdate = getDate(UserCollection.LAST_UPDATE) ?: Date(0),
        appVersionCode = getLong(UserCollection.APP_VERSION_CODE)?.toInt() ?: 0
)

fun DocumentSnapshot.toQuarter(subjects: List<Subject>) = Quarter(
        id = id,
        startDate = getDate(QuarterCollection.START_DATE) ?: Date(),
        endDate = getDate(QuarterCollection.END_DATE) ?: Date(),
        grade = getDouble(QuarterCollection.GRADE) ?: 0.0,
        gradeSum = getDouble(QuarterCollection.GRADE_SUM) ?: 0.0,
        credits = getLong(QuarterCollection.CREDITS)?.toInt() ?: 0,
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
        date = getDate(EvaluationCollection.DATE) ?: Date(),
        notes = getString(EvaluationCollection.NOTES) ?: "",
        isDone = getBoolean(EvaluationCollection.DONE) ?: false
)

/* Write to database */

fun DstEnrollment.toQuarterEntity(uid: String) = QuarterEntity(
        userId = uid,
        startDate = Timestamp(startDate),
        endDate = Timestamp(endDate),
        grade = 0.0,
        gradeSum = 0.0,
        credits = 0,
        status = STATUS_QUARTER_CURRENT
)

fun ScheduleSubject.toSubjectEntity(uid: String, qid: String) = SubjectEntity(
        userId = uid,
        quarterId = qid,
        code = code,
        name = name,
        credits = credits,
        grade = MAX_SUBJECT_GRADE,
        status = STATUS_SUBJECT_OK
)

fun QuarterEntity.quarterTitle() = (startDate.toDate() to endDate.toDate())
        .formatQuarterTitle()

fun DstQuarter.toQuarterEntity(uid: String) = QuarterEntity(
        userId = uid,
        startDate = Timestamp(startDate),
        endDate = Timestamp(endDate),
        grade = grade,
        gradeSum = gradeSum,
        credits = subjects.sumBy { it.credits },
        status = status
)

fun DstSubject.toSubjectEntity(uid: String, qid: String) = SubjectEntity(
        userId = uid,
        quarterId = qid,
        code = code,
        name = name,
        credits = credits,
        grade = grade,
        status = status.formatSubjectStatusValue()
)

fun Evaluation.toEvaluationEntity(uid: String) = EvaluationEntity(
        userId = uid,
        subjectId = sid,
        subjectCode = subjectCode,
        type = type.ordinal,
        grade = grade,
        maxGrade = maxGrade,
        date = Timestamp(date),
        notes = notes.trim().take(MAX_EVALUATION_NOTES),
        isDone = isDone
)