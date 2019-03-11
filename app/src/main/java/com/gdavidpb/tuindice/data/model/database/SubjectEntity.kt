package com.gdavidpb.tuindice.data.model.database

data class SubjectEntity(
        val userId: String,
        val quarterId: String,
        val code: String,
        val name: String,
        val credits: Int,
        val grade: Int,
        val status: Int
)