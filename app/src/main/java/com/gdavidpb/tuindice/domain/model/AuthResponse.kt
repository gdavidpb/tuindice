package com.gdavidpb.tuindice.domain.model

import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest

data class AuthResponse(
        val isSuccessful: Boolean,
        val code: AuthResponseCode,
        val message: String,
        val name: String,
        val request: AuthRequest = AuthRequest()
)