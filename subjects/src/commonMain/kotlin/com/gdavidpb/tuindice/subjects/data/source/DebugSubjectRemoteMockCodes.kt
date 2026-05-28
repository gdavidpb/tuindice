package com.gdavidpb.tuindice.subjects.data.source

internal object DebugSubjectRemoteMockCodes {
	private val subjectCodes = setOf(
		"EC5751",
		"QA",
		"QB"
	)

	fun matches(subjectCode: String): Boolean {
		return subjectCode.trim().uppercase() in subjectCodes
	}
}
