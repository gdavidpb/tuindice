package com.gdavidpb.tuindice.domain.model

import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest

data class AuthResponse(
        val request: AuthRequest = AuthRequest(),
        val name: String,
        val isSuccessful: Boolean,
        val message: String,
        val code: AuthResponseCode
)