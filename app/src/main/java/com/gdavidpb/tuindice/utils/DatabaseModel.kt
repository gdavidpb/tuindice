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
const val FIELD_USER_FIRST_NAMES = "lastNames"
const val FIELD_USER_LAST_NAMES = "firstNames"
const val FIELD_SCHOLARSHIP = "scholarship"

/* <--- Local database ---> */

/* Table names */
const val TABLE_CURRICULA = "curricula"
const val TABLE_PRIORITIES = "priorities"
const val TABLE_QUARTERS = "quarters"

/* Column names */

/* For curricula table (generics for other tables) */
const val COLUMN_ID = "id"
const val COLUMN_CAREER_CODE = "careerCode"
const val COLUMN_CODE = "code"
const val COLUMN_NAME = "name"
const val COLUMN_CREDITS = "credits"

/* For accounts table */
const val COLUMN_USB_ID = "usbId"
const val COLUMN_EMAIL = "email"
const val COLUMN_USB_UID = "uid"
const val COLUMN_FIRST_NAMES = "firstNames"
const val COLUMN_LAST_NAMES = "lastNames"
const val COLUMN_SCHOLARSHIP = "scholarship"
const val COLUMN_CAREER_NAME = "careerName"
const val COLUMN_LAST_UPDATE = "lastUpdate"
const val COLUMN_FAILED_CREDITS = "failedCredits"
const val COLUMN_FAILED_SUBJECTS = "failedSubjects"
const val COLUMN_RETIRED_CREDITS = "retiredCredits"
const val COLUMN_RETIRED_SUBJECTS = "retiredSubjects"
const val COLUMN_APPROVED_CREDITS = "approvedCredits"
const val COLUMN_APPROVED_SUBJECT = "approvedSubjects"
const val COLUMN_ENROLLED_CREDITS = "enrolledCredits"
const val COLUMN_ENROLLED_SUBJECTS = "enrolledSubjects"

/* For quarters table */
const val COLUMN_TYPE = "status"
const val COLUMN_START_TIME = "startDate"
const val COLUMN_END_TIME = "endDate"
const val COLUMN_GRADE = "grade"
const val COLUMN_GRADE_SUM = "gradeSum"

/* For subjects table */
const val COLUMN_STATUS = "status"