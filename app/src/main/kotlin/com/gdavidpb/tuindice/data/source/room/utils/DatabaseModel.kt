package com.gdavidpb.tuindice.data.source.room.utils

import com.gdavidpb.tuindice.BuildConfig

object DatabaseModel {
	const val VERSION = 1
	const val NAME = BuildConfig.APPLICATION_ID
}

object AccountTable {
	const val TABLE_NAME = "accounts"

	const val ID = "id"
	const val CARD_ID = "c_id"
	const val USB_ID = "usb_id"
	const val EMAIL = "email"
	const val FULL_NAME = "full_name"
	const val FIRST_NAMES = "first_names"
	const val LAST_NAMES = "last_names"
	const val CAREER_NAME = "career_name"
	const val CAREER_CODE = "career_code"
	const val SCHOLARSHIP = "scholarship"
	const val GRADE = "grade"
	const val ENROLLED_SUBJECTS = "enrolled_subjects"
	const val ENROLLED_CREDITS = "enrolled_credits"
	const val APPROVED_SUBJECTS = "approved_subjects"
	const val APPROVED_CREDITS = "approved_credits"
	const val RETIRED_SUBJECTS = "retired_subjects"
	const val RETIRED_CREDITS = "retired_credits"
	const val FAILED_SUBJECTS = "failed_subjects"
	const val FAILED_CREDITS = "failed_credits"
	const val LAST_UPDATE = "last_update"
}

object QuarterTable {
	const val TABLE_NAME = "quarters"

	const val ID = "id"
	const val ACCOUNT_ID = "a_id"
	const val STATUS = "status"
	const val MOCK = "mock"
	const val GRADE = "grade"
	const val GRADE_SUM = "grade_sum"
	const val CREDITS = "credits"
	const val START_DATE = "start_date"
	const val END_DATE = "end_date"
}

object SubjectTable {
	const val TABLE_NAME = "subjects"

	const val ID = "id"
	const val QUARTER_ID = "q_id"
	const val ACCOUNT_ID = "a_id"
	const val CODE = "code"
	const val NAME = "name"
	const val CREDITS = "credits"
	const val GRADE = "grade"
	const val STATUS = "status"
}

object EvaluationTable {
	const val TABLE_NAME = "evaluations"

	const val ID = "id"
	const val SUBJECT_ID = "s_id"
	const val QUARTER_ID = "q_id"
	const val ACCOUNT_ID = "a_id"
	const val SUBJECT_CODE = "subject_code"
	const val NOTES = "notes"
	const val GRADE = "grade"
	const val MAX_GRADE = "max_grade"
	const val TYPE = "type"
	const val DATE = "date"
	const val LAST_MODIFIED = "last_modified"
}