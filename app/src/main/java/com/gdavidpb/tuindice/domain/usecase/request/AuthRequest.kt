package com.gdavidpb.tuindice.domain.usecase.request

data class AuthRequest(
        val serviceUrl: String = "",
        val usbId: String = "",
        val password: String = ""
)