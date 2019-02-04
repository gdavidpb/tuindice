package com.gdavidpb.tuindice.data.mapper

import com.gdavidpb.tuindice.data.model.service.DstCredentials
import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.usecase.request.AuthRequest

open class CredentialsMapper : Mapper<AuthRequest, DstCredentials> {
    override fun map(value: AuthRequest): DstCredentials {
        return DstCredentials(usbId = value.usbId, password = value.password)
    }
}