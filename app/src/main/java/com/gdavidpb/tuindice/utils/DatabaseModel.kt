package com.gdavidpb.tuindice.utils

/* Firestore collections */

/* Users collection */
const val COLLECTION_USER = "users"

/* Users collection fields */
const val FIELD_USER_ID = "id"
const val FIELD_USER_USB_ID = "usbId"
const val FIELD_USER_TOKEN = "token"
const val FIELD_USER_EMAIL = "email"
const val FIELD_USER_FULL_NAME = "fullName"
const val FIELD_USER_FIRST_NAMES = "firstNames"
const val FIELD_USER_LAST_NAMES = "lastNames"
const val FIELD_USER_SCHOLARSHIP = "scholarship"
const val FIELD_USER_CAREER_NAME = "careerName"
const val FIELD_USER_CAREER_CODE = "careerCode"
const val FIELD_USER_GRADE = "grade"
const val FIELD_USER_PHOTO_URL = "photoUrl"
const val FIELD_USER_FAILED_CREDITS = "failedCredits"
const val FIELD_USER_FAILED_SUBJECTS = "failedSubjects"
const val FIELD_USER_RETIRED_CREDITS = "retiredCredits"
const val FIELD_USER_RETIRED_SUBJECTS = "retiredSubjects"
const val FIELD_USER_APPROVED_CREDITS = "approvedCredits"
const val FIELD_USER_APPROVED_SUBJECT = "approvedSubjects"
const val FIELD_USER_ENROLLED_CREDITS = "enrolledCredits"
const val FIELD_USER_ENROLLED_SUBJECTS = "enrolledSubjects"
const val FIELD_USER_LAST_UPDATE = "lastUpdate"
const val FIELD_USER_APP_VERSION_CODE = "appVersionCode"

/* Quarters collection */
const val COLLECTION_QUARTER = "quarters"

/* Quarters collection fields */
const val FIELD_QUARTER_USER_ID = "userId"
const val FIELD_QUARTER_START_DATE = "startDate"
const val FIELD_QUARTER_END_DATE = "endDate"
const val FIELD_QUARTER_GRADE = "grade"
const val FIELD_QUARTER_GRADE_SUM = "gradeSum"
const val FIELD_QUARTER_STATUS = "status"

/* Subjects collection */
const val COLLECTION_SUBJECT = "subjects"

/* Subjects collection fields */
const val FIELD_SUBJECT_USER_ID = "userId"
const val FIELD_SUBJECT_QUARTER_ID = "quarterId"
const val FIELD_SUBJECT_CODE = "code"
const val FIELD_SUBJECT_NAME = "name"
const val FIELD_SUBJECT_CREDITS = "credits"
const val FIELD_SUBJECT_GRADE = "grade"
const val FIELD_SUBJECT_STATUS = "status"
