package com.gdavidpb.tuindice.domain.model.service

import com.gdavidpb.tuindice.domain.model.AuthResponseCode

data class DstAuth(
        val isSuccessful: Boolean,
        val code: AuthResponseCode,
        val message: String,
        val fullName: String
)