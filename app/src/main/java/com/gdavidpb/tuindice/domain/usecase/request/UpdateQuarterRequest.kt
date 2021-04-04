package com.gdavidpb.tuindice.domain.usecase.request

data class UpdateQuarterRequest(
        val qid: String,
        val sid: String,
        val grade: Int,
        val dispatchChanges: Boolean
)
