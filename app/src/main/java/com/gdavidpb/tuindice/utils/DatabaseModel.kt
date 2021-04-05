package com.gdavidpb.tuindice.utils

/* Firestore collections */
object UserCollection {
    const val COLLECTION = "users"

    /* Fields */
    const val ID = "id"
    const val USER_ID = "userId"
    const val USB_ID = "usbId"
    const val TOKEN = "token"
    const val EMAIL = "email"
    const val FULL_NAME = "fullName"
    const val FIRST_NAMES = "firstNames"
    const val LAST_NAMES = "lastNames"
    const val SCHOLARSHIP = "scholarship"
    const val CAREER_NAME = "careerName"
    const val CAREER_CODE = "careerCode"
    const val GRADE = "grade"
    const val FAILED_CREDITS = "failedCredits"
    const val FAILED_SUBJECTS = "failedSubjects"
    const val RETIRED_CREDITS = "retiredCredits"
    const val RETIRED_SUBJECTS = "retiredSubjects"
    const val APPROVED_CREDITS = "approvedCredits"
    const val APPROVED_SUBJECT = "approvedSubjects"
    const val ENROLLED_CREDITS = "enrolledCredits"
    const val ENROLLED_SUBJECTS = "enrolledSubjects"
    const val LAST_UPDATE = "lastUpdate"
    const val APP_VERSION_CODE = "appVersionCode"
}

object QuarterCollection {
    const val COLLECTION = "quarters"

    /* Fields */
    const val USER_ID = "userId"
    const val START_DATE = "startDate"
    const val END_DATE = "endDate"
    const val GRADE = "grade"
    const val GRADE_SUM = "gradeSum"
    const val CREDITS = "credits"
    const val STATUS = "status"
}

object SubjectCollection {
    const val COLLECTION = "subjects"

    /* Fields */
    const val USER_ID = "userId"
    const val QUARTER_ID = "quarterId"
    const val CODE = "code"
    const val NAME = "name"
    const val CREDITS = "credits"
    const val GRADE = "grade"
    const val STATUS = "status"
}

object EvaluationCollection {
    const val COLLECTION = "evaluations"

    /* Fields */
    const val USER_ID = "userId"
    const val SUBJECT_ID = "subjectId"
    const val SUBJECT_CODE = "subjectCode"
    const val TYPE = "type"
    const val GRADE = "grade"
    const val MAX_GRADE = "maxGrade"
    const val DATE = "date"
    const val NOTES = "notes"
    const val DONE = "done"
}