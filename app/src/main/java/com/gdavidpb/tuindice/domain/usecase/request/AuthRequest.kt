package com.gdavidpb.tuindice.domain.usecase.request

data class AuthRequest(
        val usbId: String,
        val password: String,
        val serviceUrl: String
)