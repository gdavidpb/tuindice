package com.gdavidpb.tuindice.record.data.repository.quarter.model

data class RemoteSubject(
	val id: String,
	val quarterId: String,
	val code: String,
	val name: String,
	val credits: Int,
	val grade: Int,
	val status: Int,
	val isEditable: Boolean,
	val isRetired: Boolean,
	val isNoEffect: Boolean
)