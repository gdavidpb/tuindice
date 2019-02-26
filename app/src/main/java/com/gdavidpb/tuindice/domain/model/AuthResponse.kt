package com.gdavidpb.tuindice.domain.model

data class AuthResponse(
        val isSuccessful: Boolean,
        val code: AuthResponseCode,
        val message: String,
        val name: String
)