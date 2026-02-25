package com.gdavidpb.tuindice.record.data.repository.quarter.model

data class RemoteSubject(
	val id: String,
	val quarterId: String,
	val code: String,
	val name: String,
	val credits: Int,
	val grade: Int
)