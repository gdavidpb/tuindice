package com.gdavidpb.tuindice.domain.usecase.request

data class SignInRequest(
        val usbId: String,
        val password: String,
        val serviceUrl: String
)