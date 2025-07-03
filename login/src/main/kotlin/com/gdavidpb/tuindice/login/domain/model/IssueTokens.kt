package com.gdavidpb.tuindice.login.domain.model

data class IssueTokens(
    val uid: String,
    val email: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)