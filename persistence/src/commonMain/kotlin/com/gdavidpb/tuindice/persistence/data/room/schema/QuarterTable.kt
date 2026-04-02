package com.gdavidpb.tuindice.persistence.data.room.schema

object QuarterTable {
	const val TABLE_NAME = "quarters"

	const val ID = "quarter_id"
	const val NAME = "quarter_name"
	const val GRADE = "quarter_grade"
	const val GRADE_SUM = "quarter_grade_sum"
	const val CREDITS = "quarter_credits"
	const val CREDITS_SUM = "quarter_credits_sum"
	const val SIMULATION_GRADE = "quarter_simulation_grade"
	const val SIMULATION_GRADE_SUM = "quarter_simulation_grade_sum"
	const val SIMULATION_CREDITS = "quarter_simulation_credits"
	const val SIMULATION_CREDITS_SUM = "quarter_simulation_credits_sum"
	const val START_DATE = "quarter_start_date"
	const val END_DATE = "quarter_end_date"
	const val IS_CURRENT = "quarter_is_current"
	const val IS_READ_ONLY = "quarter_is_read_only"
	const val REVISION = "quarter_revision"
}
