package com.gdavidpb.tuindice.utils

/* <--- Remote database ---> */

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

/* Quarters collection */
const val COLLECTION_QUARTER = "quarters"

/* Subjects collection */
const val COLLECTION_SUBJECT = "subjects"

/* <--- Local database ---> */

/* Table names */
const val TABLE_PRIORITIES = "priorities"

/* Column names */

/* For quarters table */
const val COLUMN_TYPE = "status"
const val COLUMN_START_TIME = "startDate"
const val COLUMN_END_TIME = "endDate"
const val COLUMN_GRADE = "grade"
const val COLUMN_GRADE_SUM = "gradeSum"