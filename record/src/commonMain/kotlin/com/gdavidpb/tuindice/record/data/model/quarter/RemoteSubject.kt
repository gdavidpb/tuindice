package com.gdavidpb.tuindice.record.data.model.quarter

import com.gdavidpb.tuindice.base.domain.model.subject.SubjectStatus

data class RemoteSubject(
	val id: String,
	val quarterId: String,
	val code: String,
	val name: String,
	val credits: Int,
	val grade: Int,
	val status: SubjectStatus? = null,
	val revision: Long
)
