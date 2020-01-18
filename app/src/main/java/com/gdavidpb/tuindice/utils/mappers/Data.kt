package com.gdavidpb.tuindice.utils.mappers

import com.gdavidpb.tuindice.domain.model.Account
import com.gdavidpb.tuindice.domain.model.Evaluation
import com.gdavidpb.tuindice.domain.model.Quarter
import com.gdavidpb.tuindice.domain.model.Subject
import com.gdavidpb.tuindice.utils.*
import com.gdavidpb.tuindice.utils.extensions.computeCredits
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

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

fun DocumentSnapshot.toSubject() = Subject(
        id = id,
        qid = getString(FIELD_SUBJECT_QUARTER_ID) ?: "",
        code = getString(FIELD_SUBJECT_CODE) ?: "",
        name = getString(FIELD_SUBJECT_NAME) ?: "",
        credits = getLong(FIELD_SUBJECT_CREDITS)?.toInt() ?: 0,
        grade = getLong(FIELD_SUBJECT_GRADE)?.toInt() ?: MAX_GRADE,
        status = getLong(FIELD_SUBJECT_STATUS)?.toInt() ?: 0
)

fun DocumentSnapshot.toEvaluation() = Evaluation(
        id = id,
        sid = getString(FIELD_EVALUATION_SUBJECT_ID) ?: "",
        type = getLong(FIELD_EVALUATION_TYPE)?.toInt() ?: EVALUATION_OTHER,
        grade = getLong(FIELD_EVALUATION_GRADE)?.toInt() ?: 0,
        maxGrade = getLong(FIELD_EVALUATION_MAX_GRADE)?.toInt() ?: 0,
        date = getDate(FIELD_EVALUATION_DATE) ?: Date(),
        notes = getString(FIELD_EVALUATION_NOTES) ?: "",
        done = getBoolean(FIELD_EVALUATION_DONE) ?: false
)