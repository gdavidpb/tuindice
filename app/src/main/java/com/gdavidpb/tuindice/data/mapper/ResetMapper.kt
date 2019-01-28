package com.gdavidpb.tuindice.data.mapper

import android.net.Uri
import android.util.Base64
import com.gdavidpb.tuindice.domain.mapper.Mapper
import com.gdavidpb.tuindice.domain.usecase.request.ResetRequest

open class ResetMapper : Mapper<String, ResetRequest> {
    override fun map(value: String): ResetRequest {
        return Uri.parse(value).run {
            val code = getQueryParameter("oobCode")!!
            val encodedPassword = getQueryParameter("continueUrl")!!
            val decodedPassword = Base64.decode(encodedPassword, Base64.DEFAULT)
            val password = String(decodedPassword)

            ResetRequest(code, password)
        }
    }
}