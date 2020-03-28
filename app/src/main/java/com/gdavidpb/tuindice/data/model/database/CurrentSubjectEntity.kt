package com.gdavidpb.tuindice.data.model.database

data class CurrentSubjectEntity(
        val userId: String,
        val quarterId: String,
        val code: String,
        val name: String,
        val credits: Int
)