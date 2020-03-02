package com.gdavidpb.tuindice.utils.mappers

import com.gdavidpb.tuindice.data.model.database.EvaluationEntity
import com.gdavidpb.tuindice.data.model.database.QuarterEntity
import com.gdavidpb.tuindice.data.model.database.SubjectEntity
import com.gdavidpb.tuindice.data.model.database.SubjectNoGradeEntity
import com.gdavidpb.tuindice.domain.model.*
import com.gdavidpb.tuindice.domain.model.service.DstEnrollment
import com.gdavidpb.tuindice.domain.model.service.DstQuarter
import com.gdavidpb.tuindice.domain.model.service.DstSubject
import com.gdavidpb.tuindice.presentation.model.NewEvaluation
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.computeCredits
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

/* Read from database */

fun DocumentSnapshot.toAccount() = Account(
        uid = id,
        id = getString(FIELD_USER_ID) ?: "",
        usbId = getString(FIELD_USER_USB_ID) ?: "",
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
        lastUpdate = getDate(FIELD_USER_LAST_UPDATE) ?: Date(0),
        appVersionCode = getLong(FIELD_USER_APP_VERSION_CODE)?.toInt() ?: 0
)

fun DocumentSnapshot.toQuarter(subjects: List<Subject>) = Quarter(
        id = id,
        startDate = getDate(FIELD_QUARTER_START_DATE) ?: Date(),
        endDate = getDate(FIELD_QUARTER_END_DATE) ?: Date(),
        grade = getDouble(FIELD_QUARTER_GRADE) ?: 0.0,
        gradeSum = getDouble(FIELD_QUARTER_GRADE_SUM) ?: 0.0,
        credits = subjects.computeCredits(),
        status = getLong(FIELD_QUARTER_STATUS)?.toInt() ?: 0,
        subjects = subjects.toMutableList()
)

fun DocumentSnapshot.toSubject(): Subject {
    val grade = getLong(FIELD_SUBJECT_GRADE)?.toInt() ?: MAX_SUBJECT_GRADE
    val status = getLong(FIELD_SUBJECT_STATUS)?.toInt() ?: STATUS_SUBJECT_OK

    return Subject(
            id = id,
            qid = getString(FIELD_SUBJECT_QUARTER_ID) ?: "",
            code = getString(FIELD_SUBJECT_CODE) ?: "",
            name = getString(FIELD_SUBJECT_NAME) ?: "",
            credits = getLong(FIELD_SUBJECT_CREDITS)?.toInt() ?: 0,
            grade = grade,
            status = if (grade != 0) status else STATUS_SUBJECT_RETIRED
    )
}

fun DocumentSnapshot.toEvaluation() = Evaluation(
        id = id,
        sid = getString(FIELD_EVALUATION_SUBJECT_ID) ?: "",
        subjectCode = getString(FIELD_EVALUATION_SUBJECT_CODE) ?: "",
        type = EvaluationType.values()[(getLong(FIELD_EVALUATION_TYPE)?.toInt()
                ?: EvaluationType.OTHER.ordinal)],
        grade = getDouble(FIELD_EVALUATION_GRADE) ?: 0.0,
        maxGrade = getDouble(FIELD_EVALUATION_MAX_GRADE) ?: 0.0,
        date = getDate(FIELD_EVALUATION_DATE) ?: Date(),
        notes = getString(FIELD_EVALUATION_NOTES) ?: "",
        isDone = getBoolean(FIELD_EVALUATION_DONE) ?: false
)

/* Write to database */

fun NewEvaluation.toEvaluation() = Evaluation(
        id = id,
        sid = sid,
        subjectCode = subjectCode,
        type = type,
        grade = maxGrade,
        maxGrade = maxGrade,
        date = date,
        notes = notes,
        isDone = false
)

fun DstEnrollment.toQuarterEntity(uid: String) = QuarterEntity(
        userId = uid,
        startDate = Timestamp(startDate),
        endDate = Timestamp(endDate),
        grade = MAX_SUBJECT_GRADE.toDouble(),
        gradeSum = 0.0,
        status = STATUS_QUARTER_CURRENT
)

fun ScheduleSubject.toSubjectEntity(uid: String, qid: String) = SubjectEntity(
        userId = uid,
        quarterId = qid,
        code = code,
        name = name,
        credits = credits,
        grade = MAX_SUBJECT_GRADE,
        status = status.formatSubjectStatusValue()
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
        notes = notes.trim(),
        isDone = isDone
)