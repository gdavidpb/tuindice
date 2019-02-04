package com.gdavidpb.tuindice.domain.usecase.request

data class ResetRequest(
        val code: String,
        val email: String,
        val password: String
)