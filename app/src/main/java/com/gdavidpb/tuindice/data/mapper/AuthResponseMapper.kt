package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.model.service.DstAuthResponse
import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.model.AuthResponse

open class AuthResponseMapper : Mapper<DstAuthResponse, AuthResponse> {
    override fun map(value: DstAuthResponse): AuthResponse {
        return AuthResponse(message = value.message)
    }
}