package com.gdavidpb.tuindice.base.domain.model.subject

data class Subject(
	val id: String,
	val qid: String,
	val code: String,
	val name: String,
	val credits: Int,
	val grade: Int,
	val status: Int,
	val isEditable: Boolean,
	val isRetired: Boolean,
	val isNoEffect: Boolean
)