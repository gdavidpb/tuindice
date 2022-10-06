package com.gdavidpb.tuindice.base.domain.model

data class Subject(
	val id: String,
	val qid: String,
	val code: String,
	val name: String,
	val credits: Int,
	val grade: Int,
	val status: Int
)