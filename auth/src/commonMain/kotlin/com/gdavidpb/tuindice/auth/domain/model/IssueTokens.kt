package com.gdavidpb.tuindice.auth.domain.model

data class IssueTokens(
    val uid: String,
    val usbId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)