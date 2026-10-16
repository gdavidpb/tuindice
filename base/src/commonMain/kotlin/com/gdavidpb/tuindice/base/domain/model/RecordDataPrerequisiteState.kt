package com.gdavidpb.tuindice.base.domain.model

/**
 * Whether the record data is ready to be read. [hasFailed] says it could not be read for a blocking reason,
 * as it always says for a new student without a record. [isNewStudentWithoutRecord] says that the university
 * has no record for this student yet: a state of its own, which Summary tells apart from an error, because the
 * first term has not started.
 */
data class RecordDataPrerequisiteState(
	val isReady: Boolean,
	val hasFailed: Boolean,
	val isNewStudentWithoutRecord: Boolean = false
)
