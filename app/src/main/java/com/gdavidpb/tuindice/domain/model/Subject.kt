package com.gdavidpb.tuindice.domain.model

data class Subject(
        val code: String,
        val name: String,
        val credits: Int,
        val grade: Int,
        val status: String
)