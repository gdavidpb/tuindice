package com.gdavidpb.tuindice.data.model.database

data class SubjectNoGradeEntity(
        val userId: String,
        val quarterId: String,
        val code: String,
        val name: String,
        val credits: Int,
        val status: Int
)