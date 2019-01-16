package com.gdavidpb.tuindice.data.utils

/* Database result */
const val DATABASE_VERSION = 4
const val DATABASE_NAME = "database.sqlite"

/* Table names */
const val TABLE_CURRICULA = "curricula"
const val TABLE_PRIORITIES = "priorities"
const val TABLE_ACCOUNTS = "accounts"
const val TABLE_QUARTERS = "quarters"
const val TABLE_SUBJECTS = "subjects"

/* Column names */

/* For curricula table (generics for other tables) */
const val COLUMN_ID = "id"
const val COLUMN_CAREER_CODE = "careerCode"
const val COLUMN_CODE = "code"
const val COLUMN_NAME = "name"
const val COLUMN_CREDITS = "credits"

/* For priorities table */
const val COLUMN_PID = "pid"
const val COLUMN_SID = "sid"

/* For accounts table */
const val COLUMN_ACTIVE = "active"
const val COLUMN_TEMPORARY = "temporary"
const val COLUMN_USB_ID = "usbId"
const val COLUMN_USB_UID = "uid"
const val COLUMN_PASSWORD = "password"
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
const val COLUMN_AID = "aid"
const val COLUMN_TYPE = "status"
const val COLUMN_START_TIME = "startDate"
const val COLUMN_END_TIME = "endDate"
const val COLUMN_GRADE = "grade"
const val COLUMN_GRADE_SUM = "gradeSum"

/* For subjects table */
const val COLUMN_QID = "qid"
const val COLUMN_STATUS = "status"