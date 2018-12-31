package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.source.service.selector.DstAuthResponseSelector
import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.model.AuthResponse
import com.gdavidpb.tuindice.domain.model.AuthResponseCode

open class AuthResponseMapper : Mapper<DstAuthResponseSelector, AuthResponse> {
    override fun map(value: DstAuthResponseSelector): AuthResponse {
        val message = arrayOf(
                value.invalidCredentialsMessage,
                value.noEnrolledMessage,
                value.expiredSessionMessage
        ).firstOrNull {
            !it.isEmpty()
        } ?: ""

        val code = when {
            value.invalidCredentialsMessage.isNotEmpty() -> AuthResponseCode.INVALID_CREDENTIALS
            value.noEnrolledMessage.isNotEmpty() -> AuthResponseCode.NO_ENROLLED
            value.expiredSessionMessage.isNotEmpty() -> AuthResponseCode.SESSION_EXPIRED
            else -> AuthResponseCode.SUCCESS
        }

        return AuthResponse(
                isSuccessful = code == AuthResponseCode.SUCCESS || code == AuthResponseCode.NO_ENROLLED,
                name = value.fullName.substringAfter(" | "),
                message = message,
                code = code
        )
    }
}